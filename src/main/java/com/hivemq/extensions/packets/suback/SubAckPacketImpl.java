package com.hivemq.extensions.packets.suback;

import com.hivemq.extension.sdk.api.annotations.NotNull;
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

    @NotNull
    private final SUBACK subAck;

    public SubAckPacketImpl(final @NotNull SUBACK suback) {
        this.subAck = suback;
    }

    @Override
    public List<SubackReasonCode> getReasonCodes() {
        final ArrayList<SubackReasonCode> subAckReasonCodes = new ArrayList<>();
        for (final Mqtt5SubAckReasonCode code : subAck.getReasonCodes()) {
            subAckReasonCodes.add(SubackReasonCode.valueOf(code.name()));
        }
        return subAckReasonCodes;
    }

    @Override
    public int getPacketId() {
        return subAck.getPacketIdentifier();
    }

}
