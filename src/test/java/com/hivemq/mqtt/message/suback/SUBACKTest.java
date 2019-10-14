package com.hivemq.mqtt.message.suback;

import com.google.common.collect.ImmutableList;
import com.hivemq.annotations.NotNull;
import com.hivemq.configuration.service.FullConfigurationService;
import com.hivemq.extensions.packets.suback.ModifiableSubackPacketImpl;
import com.hivemq.mqtt.message.mqtt5.Mqtt5UserProperties;
import com.hivemq.mqtt.message.mqtt5.Mqtt5UserPropertiesBuilder;
import com.hivemq.mqtt.message.mqtt5.MqttUserProperty;
import com.hivemq.mqtt.message.reason.Mqtt5SubAckReasonCode;
import org.junit.Before;
import org.junit.Test;
import util.TestConfigurationBootstrap;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class SUBACKTest {

    private ModifiableSubackPacketImpl packet;

    @Before
    public void setUp() throws Exception {
        final List<Mqtt5SubAckReasonCode> originalReasonCodes = new ArrayList<>();
        originalReasonCodes.add(Mqtt5SubAckReasonCode.PACKET_IDENTIFIER_IN_USE);
        originalReasonCodes.add(Mqtt5SubAckReasonCode.IMPLEMENTATION_SPECIFIC_ERROR);
        originalReasonCodes.add(Mqtt5SubAckReasonCode.QUOTA_EXCEEDED);
        packet = createTestSubAckPacket(originalReasonCodes);

    }

    @Test
    public void test_deep_copy() {
        @NotNull final SUBACK subAckFrom = SUBACK.createSubAckFrom(packet);
        @NotNull final ImmutableList<Mqtt5SubAckReasonCode> reasonCodes = subAckFrom.getReasonCodes();
        assertEquals(subAckFrom.getPacketIdentifier(), 1);
        assertEquals(reasonCodes.get(0), Mqtt5SubAckReasonCode.PACKET_IDENTIFIER_IN_USE);
        assertEquals(reasonCodes.get(1), Mqtt5SubAckReasonCode.IMPLEMENTATION_SPECIFIC_ERROR);
        assertEquals(reasonCodes.get(2), Mqtt5SubAckReasonCode.QUOTA_EXCEEDED);
        assertEquals(subAckFrom.getReasonString(), "reasonString");
    }

    private ModifiableSubackPacketImpl createTestSubAckPacket(
            final List<Mqtt5SubAckReasonCode> reasonCodes) {
        final FullConfigurationService configurationService =
                new TestConfigurationBootstrap().getFullConfigurationService();
        final Mqtt5UserPropertiesBuilder builder =
                Mqtt5UserProperties.builder().add(new MqttUserProperty("test", "test"));
        final Mqtt5UserProperties properties = builder.build();
        final SUBACK suback = new SUBACK(1, reasonCodes, "reasonString", properties);
        return new ModifiableSubackPacketImpl(configurationService, suback);
    }
}