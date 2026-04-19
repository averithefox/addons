package me.averi.wynntils.utils

import net.minecraft.client.gui.GuiGraphics
import net.minecraft.world.item.ItemStack
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.max

private fun Int.withAlpha(alpha: Float): Int {
  val clamped = alpha.coerceIn(0f, 1f)
  val baseAlpha = (this ushr 24) and 0xFF
  val newAlpha = (baseAlpha * clamped).toInt().coerceIn(0, 255)
  return (this and 0x00FF_FFFF) or (newAlpha shl 24)
}

private fun distSqPointToRect(px: Float, py: Float, rx0: Float, ry0: Float, rx1: Float, ry1: Float): Float {
  val qx = px.coerceIn(rx0, rx1)
  val qy = py.coerceIn(ry0, ry1)
  val dx = px - qx
  val dy = py - qy
  return dx * dx + dy * dy
}

private fun maxCornerDistSqFromPoint(cx: Float, cy: Float, x: Int, y: Int): Float {
  var m = 0f
  val x0 = x.toFloat()
  val x1 = x + 1f
  val y0 = y.toFloat()
  val y1 = y + 1f
  for (px in floatArrayOf(x0, x1)) {
    for (py in floatArrayOf(y0, y1)) {
      val dx = px - cx
      val dy = py - cy
      m = max(m, dx * dx + dy * dy)
    }
  }
  return m
}

fun GuiGraphics.drawCircle(cx: Float, cy: Float, radius: Float, color: Int, ratio: Float = 1f) {
  val clampedRatio = ratio.coerceIn(0f, 1f)
  if (clampedRatio <= 0f) return

  val startAngle = -PI / 2
  val sweep = PI * 2 * clampedRatio
  val radiusR2 = radius * radius

  val pad = 1f
  val minX = (cx - radius - pad).toInt()
  val maxX = (cx + radius + pad).toInt()
  val minY = (cy - radius - pad).toInt()
  val maxY = (cy + radius + pad).toInt()

  val ss = 8
  val ssF = ss.toFloat()
  val invSs2 = 1f / (ss * ss)

  for (y in minY..maxY) {
    val py0 = y.toFloat()
    val py1 = y + 1f
    for (x in minX..maxX) {
      val px0 = x.toFloat()
      val px1 = x + 1f

      if (distSqPointToRect(cx, cy, px0, py0, px1, py1) > radiusR2) continue

      if (clampedRatio >= 1f && maxCornerDistSqFromPoint(cx, cy, x, y) <= radiusR2) {
        fill(x, y, x + 1, y + 1, color)
        continue
      }

      var acc = 0f
      for (si in 0 until ss) {
        val sx = px0 + (si * 2 + 1) / (2f * ssF)
        for (sj in 0 until ss) {
          val sy = py0 + (sj * 2 + 1) / (2f * ssF)
          val dx = sx - cx
          val dy = sy - cy
          val dsq = dx * dx + dy * dy
          if (dsq > radiusR2) continue
          if (clampedRatio < 1f) {
            val angle = atan2(dy, dx)
            val normalized = ((angle - startAngle + PI * 2) % (PI * 2))
            if (normalized > sweep) continue
          }
          acc += 1f
        }
      }
      val coverage = acc * invSs2
      if (coverage <= 0.004f) continue

      fill(x, y, x + 1, y + 1, color.withAlpha(coverage))
    }
  }
}

fun GuiGraphics.renderItem(itemStack: ItemStack, centerX: Float, centerY: Float, scale: Float) {
  pose.pushMatrix()
  pose.translate(centerX, centerY)
  pose.scale(scale, scale)
  pose.translate(-8f, -8f)
  renderItem(itemStack, 0, 0)
  pose.popMatrix()
}
