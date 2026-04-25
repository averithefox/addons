package me.averi.wynntils.events

import net.minecraft.world.entity.ItemOwner
import net.minecraft.world.item.ItemStack
import net.neoforged.bus.api.Event
import net.neoforged.bus.api.ICancellableEvent

/**
 * Published from [net.minecraft.client.renderer.item.ItemModelResolver.appendItemLayers] and [net.minecraft.client.renderer.ItemInHandRenderer.tick]
 */
class ItemModelResolveEvent(val itemStack: ItemStack, val owner: ItemOwner?) : Event(), ICancellableEvent {
  var returnValue: ItemStack = itemStack
    set(value) {
      field = value
      isCanceled = true
    }

  @Suppress("property_hides_java_field")
  var isCanceled: Boolean = false
    private set
    @JvmName($$"fox$isCanceled") get
}
