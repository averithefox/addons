package me.averi.wynntils.features

import com.mojang.blaze3d.vertex.PoseStack
import com.wynntils.core.consumers.features.ProfileDefault
import me.averi.wynntils.dx.Feature
import me.averi.wynntils.events.EntityRenderEvent
import me.averi.wynntils.events.EventBus.subscribe
import me.averi.wynntils.utils.customModel
import me.averi.wynntils.utils.mc
import net.minecraft.client.gui.Font
import net.minecraft.client.renderer.SubmitNodeCollector
import net.minecraft.client.renderer.state.CameraRenderState
import net.minecraft.network.chat.Component
import net.minecraft.world.entity.Display
import net.minecraft.world.entity.Entity

object Debug : Feature(ProfileDefault.DISABLED) {
  init {
    subscribe<EntityRenderEvent> { event ->
      onRenderEntity(event.entity, event.matrices, event.queue, event.cameraState, event.renderState.lightCoords)
    }
  }

  private fun onRenderEntity(
    entity: Entity,
    poseStack: PoseStack,
    submitNodeCollector: SubmitNodeCollector,
    cameraRenderState: CameraRenderState,
    packedLight: Int
  ) {
    if (!isEnabled) return
    if (entity !is Display.ItemDisplay) return

    val text = entity.itemStack.run { "${item}{${customModel}}" }

    poseStack.pushPose()
    poseStack.translate(0f, 2f, 0f)
    poseStack.mulPose(cameraRenderState.orientation)
    poseStack.scale(0.015f, -0.015f, 0.015f)

    submitNodeCollector.submitText(
      poseStack,
      48f,
      0f,
      Component.literal(text).getVisualOrderText(),
      false,
      Font.DisplayMode.SEE_THROUGH,
      packedLight,
      -1,
      (mc.options.getBackgroundOpacity(0.25f) * 255.0f).toInt() shl 24,
      0xFF_000000.toInt()
    )

    poseStack.popPose()
  }
}
