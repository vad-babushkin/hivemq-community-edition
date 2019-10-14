/*
 * Copyright 2019 dc-square GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.hivemq.mqtt.message.suback;

import com.google.common.collect.ImmutableList;
import com.hivemq.annotations.Immutable;
import com.hivemq.annotations.NotNull;
import com.hivemq.annotations.Nullable;
import com.hivemq.extension.sdk.api.packets.general.UserProperty;
import com.hivemq.extension.sdk.api.packets.suback.SubackPacket;
import com.hivemq.extension.sdk.api.packets.subscribe.SubackReasonCode;
import com.hivemq.mqtt.message.MessageType;
import com.hivemq.mqtt.message.mqtt5.Mqtt5UserProperties;
import com.hivemq.mqtt.message.mqtt5.MqttMessageWithUserProperties.MqttMessageWithIdAndReasonCodes;
import com.hivemq.mqtt.message.mqtt5.MqttUserProperty;
import com.hivemq.mqtt.message.reason.Mqtt5SubAckReasonCode;

import java.util.ArrayList;
import java.util.List;

/**
 * The MQTT SUBACK message
 *
 * @author Dominik Obermaier
 * @author Florian Limpöck
 * @since 1.4
 */
@Immutable
public class SUBACK extends MqttMessageWithIdAndReasonCodes<Mqtt5SubAckReasonCode> implements Mqtt3SUBACK, Mqtt5SUBACK {

    //MQTT 3
    public SUBACK(final int packetIdentifier, @NotNull final Mqtt5SubAckReasonCode... entries) {
        super(packetIdentifier, ImmutableList.copyOf(entries), null, Mqtt5UserProperties.NO_USER_PROPERTIES);
    }

    //MQTT 3
    public SUBACK(final int packetIdentifier, @NotNull final List<Mqtt5SubAckReasonCode> grantedQos) {
        this(packetIdentifier, grantedQos, null, Mqtt5UserProperties.NO_USER_PROPERTIES);
    }

    //MQTT 5
    public SUBACK(final int packetIdentifier, @NotNull final List<Mqtt5SubAckReasonCode> grantedQos, @Nullable final String reasonString) {
        this(packetIdentifier, grantedQos, reasonString, Mqtt5UserProperties.NO_USER_PROPERTIES);
    }

    //MQTT 5
    public SUBACK(final int packetIdentifier, @NotNull final List<Mqtt5SubAckReasonCode> grantedQos, @Nullable final String reasonString, @NotNull final Mqtt5UserProperties userProperties) {
        super(packetIdentifier, ImmutableList.copyOf(grantedQos), reasonString, userProperties);
    }

    //MQTT 5
    public SUBACK(final int packetIdentifier, @Nullable final String reasonString, @NotNull final Mqtt5UserProperties userProperties, @NotNull final Mqtt5SubAckReasonCode... grantedQos) {
        super(packetIdentifier, ImmutableList.copyOf(grantedQos), reasonString, userProperties);
    }

    @NotNull
    @Override
    public MessageType getType() {
        return MessageType.SUBACK;
    }

    public static @NotNull SUBACK createSubAckFrom(final @NotNull SubackPacket packet) {
        final List<Mqtt5SubAckReasonCode> subAckReasonCodes = new ArrayList<>();
        for (final SubackReasonCode code : packet.getReasonCodes()) {
            subAckReasonCodes.add(Mqtt5SubAckReasonCode.valueOf(code.name()));
        }
        final String reasonString = packet.getReasonString();
        final ImmutableList.Builder<MqttUserProperty> userPropertyBuilder = ImmutableList.builder();
        for (final UserProperty userProperty : packet.getUserProperties().asList()) {
            userPropertyBuilder.add(new MqttUserProperty(userProperty.getName(), userProperty.getValue()));
        }
        final Mqtt5UserProperties mqtt5UserProperties = Mqtt5UserProperties.of(userPropertyBuilder.build());
        return new SUBACK(packet.getPacketIdentifier(), subAckReasonCodes, reasonString, mqtt5UserProperties);
    }
}
