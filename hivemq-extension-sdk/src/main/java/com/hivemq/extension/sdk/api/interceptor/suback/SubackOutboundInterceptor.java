package com.hivemq.extension.sdk.api.interceptor.suback;

import com.hivemq.extension.sdk.api.annotations.NotNull;
import com.hivemq.extension.sdk.api.interceptor.Interceptor;
import com.hivemq.extension.sdk.api.interceptor.suback.parameter.SubackOutboundInput;
import com.hivemq.extension.sdk.api.interceptor.suback.parameter.SubackOutboundOutput;

/**
 * Interface for the subscribe outbound interception.
 * <p>
 * Interceptors are always called by the same Thread for all subscribe messages from the same client.
 *
 * If the same instance is shared between multiple clients it can be called in different Threads and must therefore be
 * thread-safe.
 *
 * @author Robin Atherton
 */
public interface SubackOutboundInterceptor extends Interceptor {

    /**
     * When a {@link SubackOutboundInterceptor} is set through any extension, this method gets called for every outbound
     * SUBACK packet from any MQTT client.
     * <p>
     * When the extension is enabled after HiveMQ is already running this method will also be called for future SubAcks
     * of clients that are already connected.
     *
     * @param subackOutboundInput  The {@link SubackOutboundInput} parameter.
     * @param subackOutboundOutput The {@link SubackOutboundOutput} parameter.
     */
    void onOutboundSuback(
            @NotNull SubackOutboundInput subackOutboundInput, @NotNull SubackOutboundOutput subackOutboundOutput);

}
