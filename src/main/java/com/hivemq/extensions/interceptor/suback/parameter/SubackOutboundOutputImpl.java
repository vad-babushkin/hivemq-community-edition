package com.hivemq.extensions.interceptor.suback.parameter;

import com.hivemq.annotations.NotNull;
import com.hivemq.configuration.service.FullConfigurationService;
import com.hivemq.extension.sdk.api.interceptor.suback.parameter.SubackOutboundOutput;
import com.hivemq.extensions.executor.PluginOutPutAsyncer;
import com.hivemq.extensions.executor.task.AbstractSimpleAsyncOutput;
import com.hivemq.extensions.packets.suback.ModifiableSubackPacketImpl;
import com.hivemq.mqtt.message.suback.SUBACK;

import java.util.function.Supplier;

/**
 * @author Robin Atherton
 */
public class SubackOutboundOutputImpl extends AbstractSimpleAsyncOutput<SubackOutboundOutput>
        implements SubackOutboundOutput,
        Supplier<SubackOutboundOutputImpl> {

    private final @NotNull FullConfigurationService configurationService;
    private @NotNull ModifiableSubackPacketImpl subAckPacket;

    public SubackOutboundOutputImpl(
            final @NotNull FullConfigurationService configurationService,
            final @NotNull PluginOutPutAsyncer asyncer,
            final @NotNull SUBACK suback) {
        super(asyncer);
        this.configurationService = configurationService;
        this.subAckPacket = new ModifiableSubackPacketImpl(configurationService, suback);
    }

    @Override
    public @NotNull ModifiableSubackPacketImpl getSubAckPacket() {
        return this.subAckPacket;
    }

    @Override
    public @NotNull SubackOutboundOutputImpl get() {
        return this;
    }

    public void update(final @NotNull ModifiableSubackPacketImpl modifiedSubAckPacket) {
        this.subAckPacket = modifiedSubAckPacket;
    }

    public void update(final @NotNull SUBACK subAck) {
        this.subAckPacket = new ModifiableSubackPacketImpl(configurationService, subAck);
    }
}
