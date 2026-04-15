package me.averi.wynntils.features

import com.mojang.blaze3d.vertex.PoseStack
import me.averi.wynntils.events.EntityRenderEvent
import me.averi.wynntils.events.EventBus.subscribe
import net.minecraft.core.component.DataComponents
import net.minecraft.world.entity.Display.ItemDisplay
import net.minecraft.world.entity.Entity
import net.minecraft.world.item.Items

object TotemMinifier {
  fun register() {
    subscribe<EntityRenderEvent> { event ->
      onRenderEntity(event.entity, event.matrices)
    }
  }

  private fun onRenderEntity(entity: Entity, matrices: PoseStack) {
    if (entity !is ItemDisplay) return
    val item = entity.itemStack
    if (!item.`is`(Items.OAK_BOAT)) return
    val customModelData = item.get(DataComponents.CUSTOM_MODEL_DATA)
    val model = customModelData?.getFloat(0) ?: return
    if (model == 30601f || model == 30602f) {
      // change shaman totem model to skyseer model
      if (model == 30601f) customModelData.floats[0] = 30602f
      val scale = 0.4f
      val offset = 1f
      matrices.translate(0.0, (-offset * (1.0f - scale)).toDouble(), 0.0)
      matrices.scale(scale, scale, scale)
    }
  }
}