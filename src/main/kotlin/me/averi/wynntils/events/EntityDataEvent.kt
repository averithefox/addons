package me.averi.wynntils.events

import net.minecraft.network.protocol.game.ClientboundSetEntityDataPacket

data class EntityDataEvent(val packet: ClientboundSetEntityDataPacket) : Event
