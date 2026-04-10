package me.averi.wynntils.events

import net.minecraft.network.protocol.game.ClientboundRemoveEntitiesPacket

data class RemoveEntitiesEvent(val packet: ClientboundRemoveEntitiesPacket) : Event
