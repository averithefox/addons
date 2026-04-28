package me.averi.wynntils.events

import net.minecraft.network.syncher.SynchedEntityData
import net.minecraft.world.entity.Entity
import net.neoforged.bus.api.Event

object EntityDataEvent {
  /**
   * Published from [net.minecraft.client.multiplayer.ClientPacketListener.handleSetEntityData] before assignValues
   */
  data class Pre(val entity: Entity, val packedItems: MutableList<SynchedEntityData.DataValue<*>>) : Event()
}
