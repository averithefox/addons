package me.averi.wynntils.features

import com.mojang.blaze3d.vertex.PoseStack
import com.wynntils.core.consumers.features.ProfileDefault
import com.wynntils.core.consumers.overlays.annotations.RegisterOverlay
import me.averi.wynntils.dx.Feature
import me.averi.wynntils.dx.Setting
import me.averi.wynntils.events.EntityRenderEvent
import me.averi.wynntils.events.EventBus.subscribe
import me.averi.wynntils.overlays.TotemTimer
import net.minecraft.core.component.DataComponents
import net.minecraft.world.entity.Display.ItemDisplay
import net.minecraft.world.entity.Entity
import net.minecraft.world.item.Items

object ShamanTotemUtils : Feature(ProfileDefault.ENABLED) {
  val changeModel by Setting(false)
  val totemScale by Setting(1f)

  @RegisterOverlay
  @Suppress("unused")
  private val totemTimerOverlay = TotemTimer

  init {
    subscribe<EntityRenderEvent> { event ->
      onRenderEntity(event.entity, event.matrices)
    }
  }

  private fun onRenderEntity(entity: Entity, matrices: PoseStack) {
    if (!isEnabled) return
    if (entity !is ItemDisplay) return
    val item = entity.itemStack
    if (!item.`is`(Items.OAK_BOAT)) return
    val customModelData = item.get(DataComponents.CUSTOM_MODEL_DATA)
    val model = customModelData?.getFloat(0) ?: return
    if (model == 30601f || model == 30602f) {
      if (model == 30601f && changeModel) customModelData.floats[0] = 30602f
      val scale = totemScale
      val offset = 1f
      matrices.translate(0.0, (-offset * (1.0f - scale)).toDouble(), 0.0)
      matrices.scale(scale, scale, scale)
    }
  }
}
