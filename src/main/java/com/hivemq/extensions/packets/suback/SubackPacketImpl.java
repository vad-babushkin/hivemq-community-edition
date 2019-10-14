package com.hivemq.extensions.packets.suback;

import com.google.common.collect.ImmutableList;
import com.hivemq.annotations.NotNull;
import com.hivemq.extension.sdk.api.packets.general.UserProperties;
import com.hivemq.extension.sdk.api.packets.suback.SubackPacket;
import com.hivemq.extension.sdk.api.packets.subscribe.SubackReasonCode;
import com.hivemq.mqtt.message.reason.Mqtt5SubAckReasonCode;
import com.hivemq.mqtt.message.suback.SUBACK;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Robin Atherton
 */
public class SubackPacketImpl implements SubackPacket {

    private final @NotNull SUBACK subAck;
    private final @NotNull ImmutableList<SubackReasonCode> subAckReasonCodes;
    private final @NotNull UserProperties userProperties;
    private final @NotNull String reasonString;

    public SubackPacketImpl(final @NotNull SUBACK subAck) {
        this.subAck = subAck;
        final ArrayList<SubackReasonCode> subAckReasonCodes = new ArrayList<>();
        for (final Mqtt5SubAckReasonCode code : this.subAck.getReasonCodes()) {
            subAckReasonCodes.add(SubackReasonCode.valueOf(code.name()));
        }
        this.subAckReasonCodes = ImmutableList.copyOf(subAckReasonCodes);
        this.userProperties = this.subAck.getUserProperties().getPluginUserProperties();
        this.reasonString = this.subAck.getReasonString();
    }

    @Override
    public @NotNull List<SubackReasonCode> getReasonCodes() {
        return subAckReasonCodes;
    }

    @Override
    public @NotNull String getReasonString() {
        return this.reasonString;
    }

    @Override
    public int getPacketIdentifier() {
        return subAck.getPacketIdentifier();
    }

    @Override
    public @NotNull UserProperties getUserProperties() {
        return userProperties;
    }

}
