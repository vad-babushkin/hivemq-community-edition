package com.hivemq.extensions.interceptor.suback.parameter;

import com.hivemq.extension.sdk.api.packets.suback.SubAckPacket;
import com.hivemq.extensions.packets.suback.SubAckPacketImpl;
import com.hivemq.mqtt.message.ProtocolVersion;
import com.hivemq.mqtt.message.mqtt5.Mqtt5UserProperties;
import com.hivemq.mqtt.message.mqtt5.MqttUserProperty;
import com.hivemq.mqtt.message.reason.Mqtt5SubAckReasonCode;
import com.hivemq.mqtt.message.suback.SUBACK;
import com.hivemq.util.ChannelAttributes;
import io.netty.channel.embedded.EmbeddedChannel;
import org.assertj.core.util.Lists;
import org.junit.Assert;
import org.junit.Test;
import util.TestMessageUtil;

import java.util.List;

public class SubAckOutboundInputImplTest {

    public static final Mqtt5UserProperties TEST_USER_PROPERTIES =
            Mqtt5UserProperties.of(new MqttUserProperty("user1", "property1"),
                    new MqttUserProperty("user2", "property2"));

    @Test
    public void test_construction() {
        final EmbeddedChannel embeddedChannel = new EmbeddedChannel();
        embeddedChannel.attr(ChannelAttributes.MQTT_VERSION).set(ProtocolVersion.MQTTv5);

        final SubAckPacket subAckPacket = new SubAckPacketImpl(TestMessageUtil.createFullMqtt5Suback());
        final SubAckOutboundInputImpl input = new SubAckOutboundInputImpl(subAckPacket, "client", embeddedChannel);
        Assert.assertNotNull(input.getClientInformation());
        Assert.assertNotNull(input.getConnectionInformation());
        Assert.assertNotNull(input.getSubAckPacket());
    }

    @Test
    public void create_SUBACK_from_package_test() {
        final SubAckPacketImpl subAckPacket = new SubAckPacketImpl(TestMessageUtil.createFullMqtt5Suback());
        final List<Mqtt5SubAckReasonCode> reasonCodes = Lists.newArrayList(
                Mqtt5SubAckReasonCode.GRANTED_QOS_0,
                Mqtt5SubAckReasonCode.GRANTED_QOS_1,
                Mqtt5SubAckReasonCode.GRANTED_QOS_2,
                Mqtt5SubAckReasonCode.IMPLEMENTATION_SPECIFIC_ERROR,
                Mqtt5SubAckReasonCode.NOT_AUTHORIZED,
                Mqtt5SubAckReasonCode.TOPIC_FILTER_INVALID,
                Mqtt5SubAckReasonCode.PACKET_IDENTIFIER_IN_USE,
                Mqtt5SubAckReasonCode.QUOTA_EXCEEDED,
                Mqtt5SubAckReasonCode.SHARED_SUBSCRIPTION_NOT_SUPPORTED,
                Mqtt5SubAckReasonCode.SUBSCRIPTION_IDENTIFIERS_NOT_SUPPORTED,
                Mqtt5SubAckReasonCode.WILDCARD_SUBSCRIPTION_NOT_SUPPORTED);

        final SUBACK subAck = new SUBACK(1, reasonCodes, "reason", TEST_USER_PROPERTIES);
        final SUBACK subAckFromPacket = SUBACK.createSubAckFrom(subAckPacket);
        Assert.assertEquals(subAck.getReasonCodes(), subAckFromPacket.getReasonCodes());
        Assert.assertEquals(subAck.getReasonString(), subAckFromPacket.getReasonString());
        Assert.assertEquals(subAck.getUserProperties(), subAckFromPacket.getUserProperties());
        Assert.assertEquals(subAck.getPacketIdentifier(), subAckFromPacket.getPacketIdentifier());
    }

    @Test(expected = NullPointerException.class)
    public void test_clientId_null() {
        final SubAckPacketImpl subAckPacket =
                new SubAckPacketImpl(TestMessageUtil.createFullMqtt5Suback());
        final SubAckOutboundInputImpl subAckOutboundInput =
                new SubAckOutboundInputImpl(subAckPacket, null, new EmbeddedChannel());
    }

    @Test(expected = NullPointerException.class)
    public void test_channel_null() {
        final SubAckPacketImpl subAckPacket =
                new SubAckPacketImpl(TestMessageUtil.createFullMqtt5Suback());
        final SubAckOutboundInputImpl subAckOutboundInput =
                new SubAckOutboundInputImpl(subAckPacket, "client", null);
    }

    @Test(expected = NullPointerException.class)
    public void test_packet_null() {
        final SubAckOutboundInputImpl disconnectInboundInput =
                new SubAckOutboundInputImpl(null, "client", new EmbeddedChannel());
    }

}