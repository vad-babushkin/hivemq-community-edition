package com.hivemq.extension.sdk.api.interceptor.suback.parameter;

import com.hivemq.extension.sdk.api.annotations.DoNotImplement;
import com.hivemq.extension.sdk.api.annotations.Immutable;
import com.hivemq.extension.sdk.api.annotations.NotNull;
import com.hivemq.extension.sdk.api.interceptor.suback.SubackOutboundInterceptor;
import com.hivemq.extension.sdk.api.packets.suback.SubackPacket;
import com.hivemq.extension.sdk.api.parameter.ClientBasedInput;

/**
 * This is the input parameter of any {@link SubackOutboundInterceptor} providing SUBACK, connection and client based
 * information.
 *
 * @author Robin Atherton
 */
@DoNotImplement
public interface SubackOutboundInput extends ClientBasedInput {

    /**
     * The unmodifiable SUBACK packet that was intercepted.
     *
     * @return An unmodifiable {@link SubackPacket}.
     */
    @NotNull
    @Immutable SubackPacket getSubAckPacket();
}
