package com.hivemq.extensions.interceptor.suback.parameter;

import com.hivemq.annotations.Immutable;
import com.hivemq.annotations.NotNull;
import com.hivemq.extension.sdk.api.client.parameter.ClientInformation;
import com.hivemq.extension.sdk.api.client.parameter.ConnectionInformation;
import com.hivemq.extension.sdk.api.interceptor.suback.parameter.SubackOutboundInput;
import com.hivemq.extension.sdk.api.packets.suback.SubackPacket;
import com.hivemq.extensions.PluginInformationUtil;
import com.hivemq.extensions.executor.task.PluginTaskInput;
import io.netty.channel.Channel;

import java.util.function.Supplier;

/**
 * @author Robin Atherton
 */
public class SubackOutboundInputImpl implements Supplier<SubackOutboundInputImpl>, SubackOutboundInput,
        PluginTaskInput {

    private final @NotNull ConnectionInformation connectionInformation;
    private final @NotNull ClientInformation clientInformation;
    private @NotNull SubackPacket subAckPacket;

    public SubackOutboundInputImpl(
            final @NotNull SubackPacket subAckPacket,
            final @NotNull String clientId,
            final @NotNull Channel channel) {
        this.subAckPacket = subAckPacket;
        this.connectionInformation = PluginInformationUtil.getAndSetConnectionInformation(channel);
        this.clientInformation = PluginInformationUtil.getAndSetClientInformation(channel, clientId);
    }

    @Override
    public @NotNull
    @Immutable
    SubackPacket getSubAckPacket() {
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
    public SubackOutboundInputImpl get() {
        return this;
    }

    public void updateSubAck(final @NotNull SubackPacket subAckPacket) {
        this.subAckPacket = subAckPacket;
    }
}
