package com.hivemq.extension.sdk.api.interceptor.puback.parameter;

import com.hivemq.extension.sdk.api.annotations.Immutable;
import com.hivemq.extension.sdk.api.annotations.NotNull;
import com.hivemq.extension.sdk.api.interceptor.puback.PubackOutboundInterceptor;
import com.hivemq.extension.sdk.api.packets.puback.PubackPacket;
import com.hivemq.extension.sdk.api.parameter.ClientBasedInput;

/**
 * This is the input parameter for any {@link PubackOutboundInterceptor}
 * providing PUBACK information.
 *
 * @author Yannick Weber
 */
public interface PubackOutboundInput extends ClientBasedInput {

    /**
     * The unmodifiable PUBACK packet that was intercepted.
     *
     * @return An unmodifiable {@link PubackPacket}.
     */
    @NotNull @Immutable PubackPacket getPubackPacket();

}