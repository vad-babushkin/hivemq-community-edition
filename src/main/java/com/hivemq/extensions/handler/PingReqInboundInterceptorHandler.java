package com.hivemq.extensions.handler;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.SettableFuture;
import com.hivemq.annotations.NotNull;
import com.hivemq.annotations.Nullable;
import com.hivemq.extension.sdk.api.async.TimeoutFallback;
import com.hivemq.extension.sdk.api.interceptor.pingrequest.PingRequestInboundInterceptor;
import com.hivemq.extensions.HiveMQExtension;
import com.hivemq.extensions.HiveMQExtensions;
import com.hivemq.extensions.classloader.IsolatedPluginClassloader;
import com.hivemq.extensions.client.ClientContextImpl;
import com.hivemq.extensions.executor.PluginOutPutAsyncer;
import com.hivemq.extensions.executor.PluginTaskExecutorService;
import com.hivemq.extensions.executor.task.PluginInOutTask;
import com.hivemq.extensions.executor.task.PluginInOutTaskContext;
import com.hivemq.extensions.interceptor.pingrequest.parameter.PingRequestInboundInputImpl;
import com.hivemq.extensions.interceptor.pingrequest.parameter.PingRequestInboundOutputImpl;
import com.hivemq.extensions.packets.pingrequest.PingRequestPacketImpl;
import com.hivemq.mqtt.message.PINGREQ;
import com.hivemq.util.ChannelAttributes;
import com.hivemq.util.Exceptions;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Singleton;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Robin Atherton
 * @since 4.2.0
 */
@ChannelHandler.Sharable
@Singleton
public class PingReqInboundInterceptorHandler extends SimpleChannelInboundHandler<PINGREQ> {

    private static final Logger log = LoggerFactory.getLogger(PingReqInboundInterceptorHandler.class);

    private final @NotNull PluginTaskExecutorService pluginTaskExecutorService;
    private final @NotNull PluginOutPutAsyncer asyncer;
    private final @NotNull HiveMQExtensions hiveMQExtensions;

    public PingReqInboundInterceptorHandler(
            final @NotNull PluginTaskExecutorService pluginTaskExecutorService,
            final @NotNull PluginOutPutAsyncer asyncer,
            final @NotNull HiveMQExtensions hiveMQExtensions) {
        this.pluginTaskExecutorService = pluginTaskExecutorService;
        this.asyncer = asyncer;
        this.hiveMQExtensions = hiveMQExtensions;
    }

    @Override
    protected void channelRead0(final @NotNull ChannelHandlerContext ctx, final @NotNull PINGREQ pingreq) throws Exception {

        final Channel channel = ctx.channel();
        if (!channel.isActive()) {
            return;
        }

        final String clientId = channel.attr(ChannelAttributes.CLIENT_ID).get();
        if (clientId == null) {
            return;
        }

        final ClientContextImpl clientContext = channel.attr(ChannelAttributes.PLUGIN_CLIENT_CONTEXT).get();
        if (clientContext == null || clientContext.getPingRequestInboundInterceptors().isEmpty()) {
            super.channelRead(ctx, pingreq);
            return;
        }

        final List<PingRequestInboundInterceptor> pingRequestInboundInterceptors = clientContext.getPingRequestInboundInterceptors();
        final PingRequestInboundOutputImpl output = new PingRequestInboundOutputImpl(asyncer, pingreq);
        final PingRequestInboundInputImpl
                input = new PingRequestInboundInputImpl(new PingRequestPacketImpl(pingreq), clientId, channel);
        final SettableFuture<Void> interceptorFuture = SettableFuture.create();
        final PingRequestInboundInterceptorContext interceptorContext =
                new PingRequestInboundInterceptorContext(PingRequestInboundInterceptorTask.class,
                        clientId, output, input, interceptorFuture, pingRequestInboundInterceptors.size());

        for (final PingRequestInboundInterceptor interceptor : pingRequestInboundInterceptors) {
            if (output.deliveryPrevented()) {
                if (!interceptorFuture.isDone()) {
                    interceptorFuture.set(null);
                }
                break;
            }

            final HiveMQExtension plugin = hiveMQExtensions.getExtensionForClassloader(
                    (IsolatedPluginClassloader) interceptor.getClass().getClassLoader());

            if  (plugin == null) {
                interceptorContext.increment();
                continue;
            }

            final PingRequestInboundInterceptorTask interceptorTask = new PingRequestInboundInterceptorTask(interceptor, plugin.getId());

            pluginTaskExecutorService.handlePluginInOutTaskExecution(interceptorContext, input, output, interceptorTask);
        }

        final InterceptorFutureCallback callback = new InterceptorFutureCallback(output, ctx);
        Futures.addCallback(interceptorFuture, callback, ctx.executor());
    }

    private static class PingRequestInboundInterceptorContext extends
            PluginInOutTaskContext<PingRequestInboundOutputImpl> {

        private final @NotNull PingRequestInboundOutputImpl output;
        private final @NotNull PingRequestInboundInputImpl input;
        private final @NotNull SettableFuture<Void> interceptorFuture;
        private final int interceptorCount;
        private final @NotNull AtomicInteger counter;

        protected PingRequestInboundInterceptorContext(final @NotNull Class<?> taskClazz,
                final @NotNull String identifier,
                final @NotNull PingRequestInboundOutputImpl output,
                final @NotNull PingRequestInboundInputImpl input,
                final @NotNull SettableFuture<Void> interceptorFuture,
                final int interceptorCount) {
            super(taskClazz, identifier);
            this.output = output;
            this.input = input;
            this.interceptorFuture = interceptorFuture;
            this.interceptorCount = interceptorCount;
            this.counter = new AtomicInteger(0);
        }

        @Override
        public void pluginPost(final @NotNull PingRequestInboundOutputImpl pluginOutput) {
            if (pluginOutput.isAsync() && pluginOutput.isTimedOut() && pluginOutput.getTimeoutFallback() == TimeoutFallback.FAILURE) {
                pluginOutput.forciblyPreventPingRequestDelivery();
            }
            if (counter.incrementAndGet() == interceptorCount || pluginOutput.deliveryPrevented()) {
                interceptorFuture.set(null);
            }
        }

        public void increment() {
            //we must set the future when no more interceptors are registered
            if (counter.incrementAndGet() == interceptorCount) {
                interceptorFuture.set(null);
            }
        }
    }

    private static class PingRequestInboundInterceptorTask implements
            PluginInOutTask<PingRequestInboundInputImpl, PingRequestInboundOutputImpl> {

        private final @NotNull PingRequestInboundInterceptor interceptor;
        private final @NotNull String pluginId;

        public PingRequestInboundInterceptorTask(final @NotNull PingRequestInboundInterceptor interceptor,
                final @NotNull String pluginId) {
            this.interceptor = interceptor;
            this.pluginId = pluginId;
        }


        @Override
        public PingRequestInboundOutputImpl apply(final @NotNull PingRequestInboundInputImpl pingRequestInboundInput,
                final @NotNull PingRequestInboundOutputImpl pingRequestInboundOutput) {
            if (pingRequestInboundOutput.deliveryPrevented()) {
                return pingRequestInboundOutput;
            }
            try {
                interceptor.onPingReq(pingRequestInboundInput, pingRequestInboundOutput);
            } catch (final Throwable e) {
                log.warn("Uncaught exception was thrown from extension with id \"{}\" on inbound ping request interception. Extensions are responsible for their own exception handling.",
                        pluginId);
                pingRequestInboundOutput.forciblyPreventPingRequestDelivery();
                Exceptions.rethrowError(e);
            }
            return pingRequestInboundOutput;
        }

        @Override
        public @NotNull ClassLoader getPluginClassLoader() {
            return interceptor.getClass().getClassLoader();
        }
    }

    private static class InterceptorFutureCallback implements FutureCallback<Void> {

        private final @NotNull PingRequestInboundOutputImpl inboundOutput;
        private final @NotNull ChannelHandlerContext ctx;

        public InterceptorFutureCallback(final @NotNull PingRequestInboundOutputImpl inboundOutput,
                final @NotNull ChannelHandlerContext ctx) {
            this.inboundOutput = inboundOutput;
            this.ctx = ctx;
        }

        @Override
        public void onSuccess(@Nullable Void result) {
            final PINGREQ pingreq = new PINGREQ();
            if (inboundOutput.deliveryPrevented()) {
                ctx.channel().close();
            } else {
                ctx.fireChannelRead(pingreq);
            }
        }

        @Override
        public void onFailure(Throwable t) {
            ctx.channel().close();
        }
    }
}
