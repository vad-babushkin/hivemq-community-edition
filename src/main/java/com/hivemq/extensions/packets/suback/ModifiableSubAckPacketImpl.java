package com.hivemq.extensions.packets.suback;

import com.hivemq.configuration.service.FullConfigurationService;
import com.hivemq.extension.sdk.api.annotations.NotNull;
import com.hivemq.extension.sdk.api.packets.general.ModifiableUserProperties;
import com.hivemq.extension.sdk.api.packets.suback.ModifiableSubAckPacket;
import com.hivemq.extension.sdk.api.packets.subscribe.SubackReasonCode;
import com.hivemq.extensions.packets.general.ModifiableUserPropertiesImpl;
import com.hivemq.mqtt.message.reason.Mqtt5SubAckReasonCode;
import com.hivemq.mqtt.message.suback.SUBACK;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Robin Atherton
 */
public class ModifiableSubAckPacketImpl implements ModifiableSubAckPacket {

    private final @NotNull ModifiableUserProperties userProperties;
    private final int packetIdentifier;

    private final boolean modified = false;
    private @NotNull List<SubackReasonCode> subAckReasonCodes = new ArrayList<>();

    public ModifiableSubAckPacketImpl(
            final @NotNull FullConfigurationService fullConfigurationService,
            final @NotNull SUBACK subAck) {
        this.userProperties = new ModifiableUserPropertiesImpl(
                subAck.getUserProperties().getPluginUserProperties(),
                fullConfigurationService.securityConfiguration().validateUTF8());
        this.packetIdentifier = subAck.getPacketIdentifier();
        final ArrayList<SubackReasonCode> subAckReasonCodes = new ArrayList<>();
        for (final Mqtt5SubAckReasonCode code : subAck.getReasonCodes()) {
            subAckReasonCodes.add(SubackReasonCode.valueOf(code.name()));
        }
    }

    @Override
    public boolean isModified() {
        return modified || userProperties.isModified();
    }

    @Override
    public void setReasonCodes(
            final List<SubackReasonCode> reasonCodes) {
        this.subAckReasonCodes = reasonCodes;
    }

    @Override
    public List<SubackReasonCode> getReasonCodes() {
        return subAckReasonCodes;
    }

    @Override
    public int getPacketId() {
        return this.packetIdentifier;
    }

    @Override
    public @NotNull ModifiableUserProperties getUserProperties() {
        return this.userProperties;
    }
}
