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
    subscribe<EntityRenderEvent> {
      onRenderEntity(entity, matrices)
    }
  }

  private fun onRenderEntity(entity: Entity, matrices: PoseStack) {
    if (entity !is ItemDisplay) return
    val item = entity.itemStack
    if (!item.`is`(Items.OAK_BOAT)) return
    val model = item.get(DataComponents.CUSTOM_MODEL_DATA)?.getFloat(0) ?: return
    if (model == 30601f) {
      val scale = 0.4f
      val offset = 1f
      matrices.translate(0.0, (-offset * (1.0f - scale)).toDouble(), 0.0)
      matrices.scale(scale, scale, scale)
    }
  }
}