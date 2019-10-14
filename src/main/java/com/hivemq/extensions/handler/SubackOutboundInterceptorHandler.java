package com.hivemq.extensions.handler;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.SettableFuture;
import com.hivemq.annotations.NotNull;
import com.hivemq.annotations.Nullable;
import com.hivemq.configuration.service.FullConfigurationService;
import com.hivemq.extension.sdk.api.interceptor.suback.SubackOutboundInterceptor;
import com.hivemq.extension.sdk.api.interceptor.suback.parameter.SubackOutboundOutput;
import com.hivemq.extensions.HiveMQExtension;
import com.hivemq.extensions.HiveMQExtensions;
import com.hivemq.extensions.classloader.IsolatedPluginClassloader;
import com.hivemq.extensions.client.ClientContextImpl;
import com.hivemq.extensions.executor.PluginOutPutAsyncer;
import com.hivemq.extensions.executor.PluginTaskExecutorService;
import com.hivemq.extensions.executor.task.PluginInOutTask;
import com.hivemq.extensions.executor.task.PluginInOutTaskContext;
import com.hivemq.extensions.interceptor.suback.parameter.SubackOutboundInputImpl;
import com.hivemq.extensions.interceptor.suback.parameter.SubackOutboundOutputImpl;
import com.hivemq.extensions.packets.suback.ModifiableSubackPacketImpl;
import com.hivemq.extensions.packets.suback.SubackPacketImpl;
import com.hivemq.mqtt.message.suback.SUBACK;
import com.hivemq.util.ChannelAttributes;
import io.netty.channel.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Robin Atherton
 */
@Singleton
@ChannelHandler.Sharable
public class SubackOutboundInterceptorHandler extends ChannelOutboundHandlerAdapter {

    private static final Logger log = LoggerFactory.getLogger(SubackOutboundInterceptorHandler.class);

    private final @NotNull FullConfigurationService configurationService;

    private final @NotNull PluginOutPutAsyncer asyncer;

    private final @NotNull HiveMQExtensions hiveMQExtensions;

    private final @NotNull PluginTaskExecutorService executorService;

    @Inject
    public SubackOutboundInterceptorHandler(
            @NotNull final FullConfigurationService configurationService,
            @NotNull final PluginOutPutAsyncer asyncer,
            @NotNull final HiveMQExtensions hiveMQExtensions,
            @NotNull final PluginTaskExecutorService executorService) {
        this.configurationService = configurationService;
        this.asyncer = asyncer;
        this.hiveMQExtensions = hiveMQExtensions;
        this.executorService = executorService;
    }

    @Override
    public void write(
            final @NotNull ChannelHandlerContext ctx, final @NotNull Object msg, final @NotNull ChannelPromise promise)
            throws Exception {

        if (!(msg instanceof SUBACK)) {
            super.write(ctx, msg, promise);
            return;
        }

        final SUBACK subAck = (SUBACK) msg;

        final Channel channel = ctx.channel();
        if (!channel.isActive()) {
            return;
        }

        final String clientId = channel.attr(ChannelAttributes.CLIENT_ID).get();
        if (clientId == null) {
            return;
        }

        final ClientContextImpl clientContext = channel.attr(ChannelAttributes.PLUGIN_CLIENT_CONTEXT).get();
        if (clientContext == null || clientContext.getSubackOutboundInterceptors().isEmpty()) {
            super.write(ctx, msg, promise);
            return;
        }

        final List<SubackOutboundInterceptor> subackOutboundInterceptors =
                clientContext.getSubackOutboundInterceptors();
        final SubackOutboundInputImpl input =
                new SubackOutboundInputImpl(new SubackPacketImpl(subAck), clientId, channel);
        final SubackOutboundOutputImpl output = new SubackOutboundOutputImpl(configurationService, asyncer, subAck);
        final SettableFuture<Void> interceptorFuture = SettableFuture.create();

        final SubAckOutboundInterceptorContext interceptorContext =
                new SubAckOutboundInterceptorContext(SubAckOutboundInterceptorTask.class, clientId, input, output,
                        interceptorFuture, subackOutboundInterceptors.size());

        for (final SubackOutboundInterceptor interceptor : subackOutboundInterceptors) {

            if (interceptorFuture.isDone()) {
                break;
            }

            final HiveMQExtension extension = hiveMQExtensions.getExtensionForClassloader(
                    (IsolatedPluginClassloader) interceptor.getClass().getClassLoader());

            if (extension == null) {
                interceptorContext.increment();
                continue;
            }

            final SubAckOutboundInterceptorTask interceptorTask =
                    new SubAckOutboundInterceptorTask(interceptor, interceptorFuture, extension.getId());
            executorService.handlePluginInOutTaskExecution(interceptorContext, input, output, interceptorTask);
        }
        final InterceptorFutureCallback callback = new InterceptorFutureCallback(output, subAck, ctx, promise);
        Futures.addCallback(interceptorFuture, callback, ctx.executor());
    }

    private static class SubAckOutboundInterceptorContext extends PluginInOutTaskContext<SubackOutboundOutputImpl> {

        private final @NotNull SubackOutboundOutputImpl output;
        private final @NotNull SubackOutboundInputImpl input;
        private final @NotNull SettableFuture<Void> interceptorFuture;
        private final int interceptorCount;
        private final @NotNull AtomicInteger counter;

        public SubAckOutboundInterceptorContext(
                @NotNull final Class<?> taskClazz,
                @NotNull final String identifier,
                @NotNull final SubackOutboundInputImpl input,
                @NotNull final SubackOutboundOutputImpl output,
                @NotNull final SettableFuture<Void> interceptorFuture,
                final int interceptorCount) {
            super(taskClazz, identifier);
            this.output = output;
            this.input = input;
            this.interceptorFuture = interceptorFuture;
            this.interceptorCount = interceptorCount;
            this.counter = new AtomicInteger(0);
        }

        @Override
        public void pluginPost(
                @NotNull final SubackOutboundOutputImpl pluginOutput) {
            if (output.isTimedOut()) {
                log.warn("Async timeout on outbound SUBACK interception.");
                final SUBACK unmodifiedSuback = SUBACK.createSubAckFrom(input.getSubAckPacket());
                output.update(unmodifiedSuback);
            } else if (pluginOutput.getSubAckPacket().isModified()) {
                @NotNull final ModifiableSubackPacketImpl subAckPacket = pluginOutput.getSubAckPacket();
                input.updateSubAck(subAckPacket);
                output.update(subAckPacket);
            }
            increment();
        }

        public void increment() {
            //we must set the future when no more interceptors are registered
            if (counter.incrementAndGet() == interceptorCount) {
                interceptorFuture.set(null);
            }
        }
    }

    private static class SubAckOutboundInterceptorTask
            implements PluginInOutTask<SubackOutboundInputImpl, SubackOutboundOutputImpl> {

        private final @NotNull SubackOutboundInterceptor interceptor;
        private final @NotNull SettableFuture<Void> interceptorFuture;
        private final @NotNull String pluginId;

        public SubAckOutboundInterceptorTask(
                @NotNull final SubackOutboundInterceptor interceptor,
                @NotNull final SettableFuture<Void> interceptorFuture,
                @NotNull final String pluginId) {
            this.interceptor = interceptor;
            this.interceptorFuture = interceptorFuture;
            this.pluginId = pluginId;
        }

        @Override
        public SubackOutboundOutputImpl apply(
                final @NotNull SubackOutboundInputImpl input,
                final @NotNull SubackOutboundOutputImpl output) {
            try {
                if (!interceptorFuture.isDone()) {
                    interceptor.onOutboundSuback(input, output);
                }
            } catch (final Throwable e) {
                log.warn(
                        "Uncaught exception was thrown from extension with id \"{}\" on outbound subAck interception. " +
                                "Extensions are responsible to handle their own exceptions.", pluginId);
                log.debug("Original exception: ", e);
                final SUBACK suback = SUBACK.createSubAckFrom(input.getSubAckPacket());
                output.update(suback);
            }
            return output;
        }

        @Override
        public @NotNull ClassLoader getPluginClassLoader() {
            return interceptor.getClass().getClassLoader();
        }
    }

    private static class InterceptorFutureCallback implements FutureCallback<Void> {

        private final @NotNull SubackOutboundOutput output;
        private final @NotNull SUBACK subAck;
        private final @NotNull ChannelHandlerContext ctx;
        private final @NotNull ChannelPromise promise;

        public InterceptorFutureCallback(
                @NotNull final SubackOutboundOutput output,
                @NotNull final SUBACK subAck,
                @NotNull final ChannelHandlerContext ctx,
                @NotNull final ChannelPromise promise) {
            this.output = output;
            this.subAck = subAck;
            this.ctx = ctx;
            this.promise = promise;
        }

        @Override
        public void onSuccess(@Nullable final Void result) {
            try {
                final SUBACK finalSubAck = SUBACK.createSubAckFrom(output.getSubAckPacket());
                ctx.writeAndFlush(finalSubAck, promise);
            } catch (final Exception e) {
                log.error("Exception while modifying an intercepted suback message.", e);
                ctx.writeAndFlush(subAck, promise);
            }
        }

        @Override
        public void onFailure(final Throwable t) {
            ctx.channel().close();
        }
    }
}
