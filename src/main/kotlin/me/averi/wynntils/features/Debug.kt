package me.averi.wynntils.features

import com.mojang.blaze3d.vertex.PoseStack
import com.wynntils.core.consumers.features.ProfileDefault
import me.averi.wynntils.dx.Feature
import me.averi.wynntils.events.EntityRenderEvent
import me.averi.wynntils.events.EventBus.subscribe
import me.averi.wynntils.utils.mc
import net.minecraft.client.gui.Font
import net.minecraft.client.renderer.SubmitNodeCollector
import net.minecraft.client.renderer.state.CameraRenderState
import net.minecraft.core.component.DataComponents
import net.minecraft.network.chat.Component
import net.minecraft.util.FormattedCharSequence
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
    var text = "${entity.type}"
    if (entity is Display.ItemDisplay) {
      val item = entity.itemStack
      text += "\n${item.item}{${item.damageValue},${item.get(DataComponents.CUSTOM_MODEL_DATA)?.getFloat(0)}}"
    }

    val lines = text.split("\n")

    poseStack.pushPose()
    poseStack.translate(0f, 2f, 0f)
    poseStack.mulPose(cameraRenderState.orientation)
    poseStack.scale(0.015f, -0.015f, 0.015f)

    val k = (mc.options.getBackgroundOpacity(0.25f) * 255.0f).toInt() shl 24

    lines.forEachIndexed { index, line ->
      val charSequence: FormattedCharSequence = Component.literal(line).getVisualOrderText()

      submitNodeCollector.submitText(
        poseStack,
        48f,
        index * 10.0f,
        charSequence,
        false, // dropShadow
        Font.DisplayMode.SEE_THROUGH,
        packedLight, // light
        -1, // color
        k, // background color
        0xff000000.toInt() // outline color
      )
    }

    poseStack.popPose()
  }
}
