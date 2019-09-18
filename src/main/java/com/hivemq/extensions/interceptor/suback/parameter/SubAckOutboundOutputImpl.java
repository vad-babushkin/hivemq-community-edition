package com.hivemq.extensions.interceptor.suback.parameter;

import com.hivemq.configuration.service.FullConfigurationService;
import com.hivemq.extension.sdk.api.annotations.NotNull;
import com.hivemq.extension.sdk.api.interceptor.suback.parameter.SubAckOutboundOutput;
import com.hivemq.extension.sdk.api.packets.suback.ModifiableSubAckPacket;
import com.hivemq.extensions.executor.PluginOutPutAsyncer;
import com.hivemq.extensions.executor.task.AbstractSimpleAsyncOutput;
import com.hivemq.extensions.packets.suback.ModifiableSubAckPacketImpl;
import com.hivemq.mqtt.message.suback.SUBACK;

import java.util.function.Supplier;

/**
 * @author Robin Atherton
 */
public class SubAckOutboundOutputImpl extends AbstractSimpleAsyncOutput<SubAckOutboundOutput>
        implements SubAckOutboundOutput,
        Supplier<SubAckOutboundOutputImpl> {

    private final @NotNull FullConfigurationService configurationService;
    private @NotNull ModifiableSubAckPacketImpl subAckPacket;

    public SubAckOutboundOutputImpl(
            final @NotNull FullConfigurationService configurationService,
            final @NotNull PluginOutPutAsyncer asyncer,
            final @NotNull SUBACK suback) {
        super(asyncer);
        this.configurationService = configurationService;
        this.subAckPacket = new ModifiableSubAckPacketImpl(configurationService, suback);
    }

    @Override
    public @NotNull ModifiableSubAckPacket getSubAckPacket() {
        return this.subAckPacket;
    }

    @Override
    public SubAckOutboundOutputImpl get() {
        return this;
    }

    public void update(final @NotNull ModifiableSubAckPacketImpl modifiedSubAckPacket) {
        this.subAckPacket = modifiedSubAckPacket;
    }

    public void update(final @NotNull SUBACK subAck) {
        this.subAckPacket = new ModifiableSubAckPacketImpl(configurationService, subAck);
    }
}
