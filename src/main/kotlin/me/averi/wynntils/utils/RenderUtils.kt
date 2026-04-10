package me.averi.wynntils.utils

import net.minecraft.client.gui.GuiGraphics
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.sqrt

private fun Int.withAlpha(alpha: Float): Int {
  val clamped = alpha.coerceIn(0f, 1f)
  val baseAlpha = (this ushr 24) and 0xFF
  val newAlpha = (baseAlpha * clamped).toInt().coerceIn(0, 255)
  return (this and 0x00FF_FFFF) or (newAlpha shl 24)
}

fun GuiGraphics.drawCircle(cx: Float, cy: Float, radius: Float, color: Int, ratio: Float = 1f) {
  val clampedRatio = ratio.coerceIn(0f, 1f)
  if (clampedRatio <= 0f) return

  val startAngle = -PI / 2
  val sweep = PI * 2 * clampedRatio
  val innerRadius = (radius - 1.25f).coerceAtLeast(0f)
  val outerRadius = radius + 0.35f
  val innerR2 = innerRadius * innerRadius
  val outerR2 = outerRadius * outerRadius
  val outlineColor = color.withAlpha(0.45f)

  val minX = (cx - outerRadius).toInt()
  val maxX = (cx + outerRadius).toInt()
  val minY = (cy - outerRadius).toInt()
  val maxY = (cy + outerRadius).toInt()

  for (y in minY..maxY) {
    for (x in minX..maxX) {
      val dx = x + 0.5f - cx
      val dy = y + 0.5f - cy
      val distanceSq = dx * dx + dy * dy

      if (distanceSq > outerR2) continue
      if (clampedRatio < 1f) {
        val angle = atan2(dy, dx)
        val normalized = ((angle - startAngle + PI * 2) % (PI * 2))
        if (normalized > sweep) continue
      }

      if (distanceSq <= innerR2) {
        fill(x, y, x + 1, y + 1, color)
        continue
      }

      val distance = sqrt(distanceSq)
      val edgeAlpha = ((outerRadius - distance) / (outerRadius - innerRadius)).coerceIn(0f, 1f)
      fill(x, y, x + 1, y + 1, outlineColor.withAlpha(edgeAlpha))
    }
  }
}
