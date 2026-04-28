package me.averi.wynntils.utils.render

import com.mojang.blaze3d.vertex.PoseStack
import me.averi.wynntils.utils.mc
import me.averi.wynntils.utils.setColor
import net.minecraft.world.phys.Vec3
import java.awt.Color

inline fun RenderContext.withWorldTranslation(block: RenderContext.(matrix: PoseStack.Pose) -> Unit) {
  val cameraPos = mc.gameRenderer.mainCamera.position()
  matrices.pushPose()
  matrices.translate(-cameraPos.x, -cameraPos.y, -cameraPos.z)

  block(matrices.last())

  matrices.popPose()
}

fun RenderContext.renderLine(from: Vec3, to: Vec3, color: Color, thickness: Float = 2f) =
  withWorldTranslation { matrix ->
    val buffer = consumers.getBuffer(LINES_THROUGH_WALLS)
    val dir = to.subtract(from).normalize().toVector3f()

    buffer.addVertex(matrix, from.toVector3f()).setColor(color).setNormal(matrix, dir).setLineWidth(thickness)
    buffer.addVertex(matrix, to.toVector3f()).setColor(color).setNormal(matrix, dir).setLineWidth(thickness)
  }
