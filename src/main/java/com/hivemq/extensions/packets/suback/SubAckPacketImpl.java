package com.hivemq.extensions.packets.suback;

import com.hivemq.extension.sdk.api.annotations.NotNull;
import com.hivemq.extension.sdk.api.packets.general.UserProperties;
import com.hivemq.extension.sdk.api.packets.suback.SubAckPacket;
import com.hivemq.extension.sdk.api.packets.subscribe.SubackReasonCode;
import com.hivemq.mqtt.message.reason.Mqtt5SubAckReasonCode;
import com.hivemq.mqtt.message.suback.SUBACK;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Robin Atherton
 */
public class SubAckPacketImpl implements SubAckPacket {

    private final @NotNull SUBACK subAck;
    private final @NotNull List<SubackReasonCode> subAckReasonCodes;
    private final UserProperties userProperties;
    private final String reasonString;

    public SubAckPacketImpl(final @NotNull SUBACK subAck) {
        this.subAck = subAck;
        this.subAckReasonCodes = new ArrayList<>();
        for (final Mqtt5SubAckReasonCode code : this.subAck.getReasonCodes()) {
            subAckReasonCodes.add(SubackReasonCode.valueOf(code.name()));
        }
        this.userProperties = this.subAck.getUserProperties().getPluginUserProperties();
        this.reasonString = this.subAck.getReasonString();
    }

    @Override
    public List<SubackReasonCode> getReasonCodes() {
        return subAckReasonCodes;
    }

    @Override
    public String getReasonString() {
        return this.subAck.getReasonString();
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
