package com.hivemq.extension.sdk.api.packets.suback;

import com.hivemq.extension.sdk.api.annotations.NotNull;
import com.hivemq.extension.sdk.api.packets.general.UserProperties;
import com.hivemq.extension.sdk.api.packets.subscribe.SubackReasonCode;

import java.util.List;

/**
 * Represents a SUBACK packet.
 * <p>
 * Contains all values of an MQTT 5 SUBACK, but will also be used to represent MQTT 3 suback messages.
 *
 * @author Robin Atherton
 */
public interface SubackPacket {

    /**
     * Represents the return codes for the QoS levels of the different Topics contained in the corresponding SUBSCRIBE
     * message as well as potential failure codes.
     *
     * @return The reason codes for the subscribed topics.
     */
    @NotNull List<SubackReasonCode> getReasonCodes();

    /**
     * Represents the return codes for the QoS levels of the different Topics contained in the corresponding SUBSCRIBE
     * message as well as potential failure codes as a String. Made for user analysis.
     *
     * @return The reason codes as a String.
     */
    @NotNull String getReasonString();

    /**
     * The packet identifier of the SUBSCRIBE packet.
     *
     * @return The packet identifier.
     * @since 4.2.0
     */
    int getPacketIdentifier();

    /**
     * The user properties from the DISCONNECT packet.
     *
     * @return The {@link UserProperties} of the DISCONNECT packet.
     */
    @NotNull UserProperties getUserProperties();
}
