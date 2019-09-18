package com.hivemq.extension.sdk.api.interceptor.suback;

import com.hivemq.extension.sdk.api.annotations.NotNull;
import com.hivemq.extension.sdk.api.interceptor.Interceptor;
import com.hivemq.extension.sdk.api.interceptor.suback.parameter.SubAckOutboundInput;
import com.hivemq.extension.sdk.api.interceptor.suback.parameter.SubAckOutboundOutput;

/**
 * Represents a SUBACK packet.
 *
 * @author Robin Atherton
 */
public interface SubAckOutboundInterceptor extends Interceptor {

    /**
     * When a {@link SubAckOutboundInterceptor} is set through any extension, this method gets called for every outbound
     * SUBACK packet from any MQTT client.
     * <p>
     * When the extension is enabled after HiveMQ is already running this method will also be called for future SubAcks
     * of clients that are already connected.
     *
     * @param subAckOutboundInput  The {@link SubAckOutboundInput} parameter.
     * @param subAckOutboundOutput The {@link SubAckOutboundOutput} parameter.
     */
    void onOutboundSubAck(
            @NotNull SubAckOutboundInput subAckOutboundInput, @NotNull SubAckOutboundOutput subAckOutboundOutput);

}
