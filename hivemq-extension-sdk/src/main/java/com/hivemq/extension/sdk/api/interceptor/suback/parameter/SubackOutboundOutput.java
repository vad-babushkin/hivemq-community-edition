package com.hivemq.extension.sdk.api.interceptor.suback.parameter;

import com.hivemq.extension.sdk.api.annotations.NotNull;
import com.hivemq.extension.sdk.api.async.Async;
import com.hivemq.extension.sdk.api.async.SimpleAsyncOutput;
import com.hivemq.extension.sdk.api.interceptor.suback.SubackOutboundInterceptor;
import com.hivemq.extension.sdk.api.packets.suback.ModifiableSubackPacket;

import java.time.Duration;

/**
 * This is the output parameter of any {@link SubackOutboundInterceptor} providing methods to define the outcome of
 * SUBACK interception.
 * <p>
 * It can be used to modify an outbound SUBACK packet.
 * <p>
 *
 * @author Robin Atherton
 **/
public interface SubackOutboundOutput extends SimpleAsyncOutput<SubackOutboundOutput> {

    /**
     * Use this object to make any changes to the outbound SUBACK.
     *
     * @return A {@link ModifiableSubackPacket}
     */
    @NotNull
    ModifiableSubackPacket getSubAckPacket();

    /**
     * If the timeout is expired before {@link Async#resume()} is called then the outcome is handled as failed. This
     * means that the outcome results an unmodified SUBACK is sent to the server.
     * <p>
     * Do not call this method more than once. If an async method is called multiple times an exception is thrown.
     *
     * @param timeout Timeout that HiveMQ waits for the result of the async operation.
     * @throws UnsupportedOperationException If async is called more than once.
     */
    @Override
    @NotNull Async<SubackOutboundOutput> async(@NotNull Duration timeout);

}
