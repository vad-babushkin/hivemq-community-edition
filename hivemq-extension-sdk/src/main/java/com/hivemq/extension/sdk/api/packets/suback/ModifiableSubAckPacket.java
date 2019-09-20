package com.hivemq.extension.sdk.api.packets.suback;

import com.hivemq.extension.sdk.api.annotations.NotNull;
import com.hivemq.extension.sdk.api.packets.general.ModifiableUserProperties;
import com.hivemq.extension.sdk.api.packets.general.UserProperties;
import com.hivemq.extension.sdk.api.packets.subscribe.SubackReasonCode;

import java.util.List;

public interface ModifiableSubAckPacket extends SubAckPacket {

    /**
     * Sets the reasonString for the SUBACK packet.
     *
     * @param reasonString the reason to be set as a String.
     */
    void setReasonString(String reasonString);

    /**
     * Method to manipulate the reason Codes of the SUBACK packet.
     *
     * @param reasonCodes the list of reasonCodes to be written into the SUBACK packet.
     */
    void setReasonCodes(final List<SubackReasonCode> reasonCodes);

    /**
     * Gets the modifiable {@link UserProperties} of the SUBACK packet.
     *
     * @return Modifiable user properties.
     */
    @Override
    @NotNull ModifiableUserProperties getUserProperties();

}
