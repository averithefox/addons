package me.averi.wynntils.utils.render

import com.mojang.blaze3d.vertex.PoseStack
import me.averi.wynntils.utils.f
import me.averi.wynntils.utils.mc
import me.averi.wynntils.utils.setColor
import net.minecraft.world.phys.Vec3
import java.awt.Color
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

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

fun RenderContext.renderFilledCircle(center: Vec3, radius: Float, color: Color, segments: Int = 64, throughWalls: Boolean = false) =
  withWorldTranslation { matrix ->
    val buffer = consumers.getBuffer(if (throughWalls) FILLED_TRIANGLE_FAN_THROUGH_WALLS else FILLED_TRIANGLE_FAN)
    val angleStep = 2f * PI / segments

    buffer.addVertex(matrix, center.toVector3f()).setColor(color)

    for (i in (segments downTo 0) + (1..segments)) {
      val angle = i * angleStep
      val x = center.x + radius * cos(angle)
      val z = center.z + radius * sin(angle)

      buffer.addVertex(matrix, x.f, center.y.f, z.f).setColor(color)
    }
  }
