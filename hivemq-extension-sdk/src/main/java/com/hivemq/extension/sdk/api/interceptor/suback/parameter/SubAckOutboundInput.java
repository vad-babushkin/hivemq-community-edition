package com.hivemq.extension.sdk.api.interceptor.suback.parameter;

import com.hivemq.extension.sdk.api.annotations.DoNotImplement;
import com.hivemq.extension.sdk.api.annotations.Immutable;
import com.hivemq.extension.sdk.api.annotations.NotNull;
import com.hivemq.extension.sdk.api.interceptor.suback.SubAckOutboundInterceptor;
import com.hivemq.extension.sdk.api.packets.suback.SubAckPacket;
import com.hivemq.extension.sdk.api.packets.subscribe.SubscribePacket;
import com.hivemq.extension.sdk.api.parameter.ClientBasedInput;

/**
 * This is the input parameter of any {@link SubAckOutboundInterceptor} providing SUBACK, conenction and client based
 * information.
 *
 * @author Robin Atherton
 */
@DoNotImplement
public interface SubAckOutboundInput extends ClientBasedInput {

    /**
     * The unmodifiable SUBACK packet that was intercepted.
     *
     * @return An unmodifiable {@link SubAckPacket}.
     */
    @NotNull
    @Immutable SubscribePacket getSubAck();
}
