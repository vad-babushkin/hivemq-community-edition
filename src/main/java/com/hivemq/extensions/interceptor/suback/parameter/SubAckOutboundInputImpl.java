package com.hivemq.extensions.interceptor.suback.parameter;

import com.hivemq.extension.sdk.api.annotations.Immutable;
import com.hivemq.extension.sdk.api.annotations.NotNull;
import com.hivemq.extension.sdk.api.client.parameter.ClientInformation;
import com.hivemq.extension.sdk.api.client.parameter.ConnectionInformation;
import com.hivemq.extension.sdk.api.interceptor.suback.parameter.SubAckOutboundInput;
import com.hivemq.extension.sdk.api.packets.suback.SubAckPacket;
import com.hivemq.extensions.PluginInformationUtil;
import com.hivemq.extensions.executor.task.PluginTaskInput;
import io.netty.channel.Channel;

import java.util.function.Supplier;

/**
 * @author Robin Atherton
 */
public class SubAckOutboundInputImpl implements Supplier<SubAckOutboundInputImpl>, SubAckOutboundInput,
        PluginTaskInput {

    private final @NotNull ConnectionInformation connectionInformation;
    private final @NotNull ClientInformation clientInformation;
    private @NotNull SubAckPacket subAckPacket;

    public SubAckOutboundInputImpl(
            final @NotNull SubAckPacket subAckPacket,
            final @NotNull String clientId,
            final @NotNull Channel channel) {
        this.subAckPacket = subAckPacket;
        this.connectionInformation = PluginInformationUtil.getAndSetConnectionInformation(channel);
        this.clientInformation = PluginInformationUtil.getAndSetClientInformation(channel, clientId);
    }

    @Override
    public @NotNull @Immutable SubAckPacket getSubAckPacket() {
        return subAckPacket;
    }

    @Override
    public @NotNull ConnectionInformation getConnectionInformation() {
        return connectionInformation;
    }

    @Override
    public @NotNull ClientInformation getClientInformation() {
        return clientInformation;
    }

    @Override
    public SubAckOutboundInputImpl get() {
        return this;
    }

    public void updateSubAck(final @NotNull SubAckPacket subAckPacket) {
        this.subAckPacket = subAckPacket;
    }
}
