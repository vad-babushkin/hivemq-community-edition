package com.hivemq.extension.sdk.api.interceptor.pingrequest;

import com.hivemq.extension.sdk.api.annotations.NotNull;
import com.hivemq.extension.sdk.api.interceptor.Interceptor;
import com.hivemq.extension.sdk.api.interceptor.pingrequest.parameter.PingRequestInboundInput;
import com.hivemq.extension.sdk.api.interceptor.pingrequest.parameter.PingRequestInboundOutput;

/**
 * @author Robin Atherton
 * @since 4.2.0
 */
public interface PingRequestInboundInterceptor extends Interceptor {

    /**
     * When a {@link PingRequestInboundInterceptor} is set through any extension,
     * this method gets called for every inbound PINGREQ packet from any MQTT client.
     *
     * @param pingRequestInboundInput  The {@link PingRequestInboundInput} parameter.
     * @param pingRequestInboundOutput The {@link PingRequestInboundOutput} parameter.
     * @since 4.2.0
     */
    void onPingReq(@NotNull PingRequestInboundInput pingRequestInboundInput, @NotNull PingRequestInboundOutput pingRequestInboundOutput);

}