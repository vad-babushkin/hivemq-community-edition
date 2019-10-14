package com.hivemq.extensions.handler;

import com.google.common.collect.ImmutableList;
import com.hivemq.common.shutdown.ShutdownHooks;
import com.hivemq.configuration.service.FullConfigurationService;
import com.hivemq.extension.sdk.api.annotations.NotNull;
import com.hivemq.extension.sdk.api.interceptor.unsubscribe.UnsubscribeInboundInterceptor;
import com.hivemq.extension.sdk.api.interceptor.unsubscribe.parameter.UnsubscribeInboundInput;
import com.hivemq.extension.sdk.api.interceptor.unsubscribe.parameter.UnsubscribeInboundOutput;
import com.hivemq.extension.sdk.api.packets.unsubscribe.ModifiableUnsubscribePacket;
import com.hivemq.extensions.HiveMQExtension;
import com.hivemq.extensions.HiveMQExtensions;
import com.hivemq.extensions.classloader.IsolatedPluginClassloader;
import com.hivemq.extensions.client.ClientContextImpl;
import com.hivemq.extensions.executor.PluginOutPutAsyncer;
import com.hivemq.extensions.executor.PluginOutputAsyncerImpl;
import com.hivemq.extensions.executor.PluginTaskExecutorService;
import com.hivemq.extensions.executor.PluginTaskExecutorServiceImpl;
import com.hivemq.extensions.executor.task.PluginTaskExecutor;
import com.hivemq.extensions.packets.general.ModifiableDefaultPermissionsImpl;
import com.hivemq.mqtt.message.ProtocolVersion;
import com.hivemq.mqtt.message.mqtt5.Mqtt5UserProperties;
import com.hivemq.mqtt.message.unsubscribe.UNSUBSCRIBE;
import com.hivemq.util.ChannelAttributes;
import io.netty.channel.embedded.EmbeddedChannel;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.exporter.ZipExporter;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.*;
import org.junit.rules.TemporaryFolder;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import util.TestConfigurationBootstrap;

import java.io.File;
import java.net.URL;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

public class UnsubscribeInboundInterceptorHandlerTest {

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    @Mock
    private HiveMQExtension extension;

    @Mock
    private HiveMQExtensions extensions;

    @Mock
    private ClientContextImpl clientContext;

    @Mock
    private FullConfigurationService configurationService;

    private PluginTaskExecutor executor;
    private EmbeddedChannel channel;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);

        executor = new PluginTaskExecutor(new AtomicLong());
        executor.postConstruct();

        channel = new EmbeddedChannel();
        channel.attr(ChannelAttributes.CLIENT_ID).set("client");
        channel.attr(ChannelAttributes.REQUEST_RESPONSE_INFORMATION).set(true);
        channel.attr(ChannelAttributes.PLUGIN_CLIENT_CONTEXT).set(clientContext);
        when(extension.getId()).thenReturn("extension");

        configurationService = new TestConfigurationBootstrap().getFullConfigurationService();
        final PluginOutPutAsyncer asyncer = new PluginOutputAsyncerImpl(Mockito.mock(ShutdownHooks.class));
        final PluginTaskExecutorService pluginTaskExecutorService = new PluginTaskExecutorServiceImpl(() -> executor);

        final UnsubscribeInboundInterceptorHandler handler =
                new UnsubscribeInboundInterceptorHandler(configurationService, asyncer, extensions,
                        pluginTaskExecutorService);
        channel.pipeline().addFirst(handler);
    }

    @After
    public void tearDown() {
        executor.stop();
        channel.close();
    }

    @Test
    public void test_simple_intercept() throws Exception {
        final ClientContextImpl clientContext =
                new ClientContextImpl(extensions, new ModifiableDefaultPermissionsImpl());

        final UnsubscribeInboundInterceptor interceptor =
                getIsolatedInboundInterceptor("SimpleUnsubscribeTestInterceptor");
        clientContext.addUnsubscribeInboundInterceptor(interceptor);

        channel.attr(ChannelAttributes.PLUGIN_CLIENT_CONTEXT).set(clientContext);
        channel.attr(ChannelAttributes.MQTT_VERSION).set(ProtocolVersion.MQTTv3_1);

        when(extensions.getExtensionForClassloader(any(IsolatedPluginClassloader.class))).thenReturn(extension);

        channel.writeInbound(testUnsubscribe());
        UNSUBSCRIBE unsubscribe = channel.readInbound();
        while (unsubscribe == null) {
            channel.runPendingTasks();
            channel.runScheduledPendingTasks();
            unsubscribe = channel.readInbound();
        }
        Assert.assertNotNull(unsubscribe);
    }

    @Test
    public void test_modifying_topics() throws Exception {
        final ClientContextImpl clientContext =
                new ClientContextImpl(extensions, new ModifiableDefaultPermissionsImpl());

        final UnsubscribeInboundInterceptor interceptor =
                getIsolatedInboundInterceptor("ModifyUnsubscribeTestInterceptor");
        clientContext.addUnsubscribeInboundInterceptor(interceptor);

        channel.attr(ChannelAttributes.PLUGIN_CLIENT_CONTEXT).set(clientContext);
        channel.attr(ChannelAttributes.MQTT_VERSION).set(ProtocolVersion.MQTTv3_1);

        when(extensions.getExtensionForClassloader(any(IsolatedPluginClassloader.class))).thenReturn(extension);

        channel.writeInbound(testUnsubscribe());
        UNSUBSCRIBE unsubscribe = channel.readInbound();
        while (unsubscribe == null) {
            channel.runPendingTasks();
            channel.runScheduledPendingTasks();
            unsubscribe = channel.readInbound();
        }
        assertEquals(Collections.singletonList("not topics"), unsubscribe.getTopics());
    }

    private @NotNull UNSUBSCRIBE testUnsubscribe() {
        return new UNSUBSCRIBE(ImmutableList.of("topics"), 1, Mqtt5UserProperties.NO_USER_PROPERTIES);
    }

    private UnsubscribeInboundInterceptor getIsolatedInboundInterceptor(final @NotNull String name) throws Exception {
        final JavaArchive javaArchive = ShrinkWrap.create(JavaArchive.class)
                .addClass("com.hivemq.extensions.handler.UnsubscribeInboundInterceptorHandlerTest$" + name);

        final File jarFile = temporaryFolder.newFile();
        javaArchive.as(ZipExporter.class).exportTo(jarFile, true);

        final IsolatedPluginClassloader
                cl =
                new IsolatedPluginClassloader(new URL[]{jarFile.toURI().toURL()}, this.getClass().getClassLoader());

        final Class<?> interceptorClass =
                cl.loadClass("com.hivemq.extensions.handler.UnsubscribeInboundInterceptorHandlerTest$" + name);

        return (UnsubscribeInboundInterceptor) interceptorClass.newInstance();
    }

    public static class SimpleUnsubscribeTestInterceptor implements UnsubscribeInboundInterceptor {

        @Override
        public void onInboundUnsubscribe(
                final @NotNull UnsubscribeInboundInput input,
                final @NotNull UnsubscribeInboundOutput output) {
            System.out.println("Intercepting UNSUBSCRIBE at:" + System.currentTimeMillis());
        }
    }

    public static class ModifyUnsubscribeTestInterceptor implements UnsubscribeInboundInterceptor {

        @Override
        public void onInboundUnsubscribe(
                final @NotNull UnsubscribeInboundInput input,
                final @NotNull UnsubscribeInboundOutput output) {
            final ModifiableUnsubscribePacket packet = output.getUnsubscribePacket();
            packet.setTopics(Collections.singletonList("not topics"));
        }
    }

}