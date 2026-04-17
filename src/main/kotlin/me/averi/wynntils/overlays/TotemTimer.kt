package me.averi.wynntils.overlays

import com.mojang.blaze3d.platform.Window
import com.wynntils.core.components.Models
import com.wynntils.core.consumers.overlays.OverlayPosition
import com.wynntils.models.abilities.event.TotemEvent
import com.wynntils.utils.render.type.HorizontalAlignment
import com.wynntils.utils.render.type.VerticalAlignment
import me.averi.wynntils.dx.Overlay
import me.averi.wynntils.events.EventBus.subscribe
import me.averi.wynntils.utils.drawCircle
import me.averi.wynntils.utils.mc
import net.minecraft.client.DeltaTracker
import net.minecraft.client.gui.GuiGraphics
import kotlin.math.max

object TotemTimer : Overlay(
  OverlayPosition(
    -100f, 0f, VerticalAlignment.BOTTOM, HorizontalAlignment.CENTER, OverlayPosition.AnchorSection.BOTTOM_MIDDLE
  ), 80f + 15f, 20f
) {
  private var totemDuration = -1
  private val displayRatios = mutableMapOf<Int, Float>()

  init {
    subscribe(::onTotemActivate)
    subscribe<TotemEvent.Removed> { onTotemRemove() }
  }

  override fun render(ctx: GuiGraphics, deltaTracker: DeltaTracker, window: Window) {
    val timers = Models.ShamanTotem.activeTotems.map { it.time }.filter { it != -1 }
    renderFor(timers, totemDuration, displayRatios, ctx, deltaTracker)
  }

  override fun renderPreview(ctx: GuiGraphics, deltaTracker: DeltaTracker, window: Window) {
    renderFor(listOf(30, 22, 15, 7), 30, mutableMapOf(), ctx, deltaTracker)
  }

  private fun renderFor(
    timers: List<Int>,
    totemDuration: Int,
    displayRatios: MutableMap<Int, Float>,
    ctx: GuiGraphics,
    deltaTracker: DeltaTracker
  ) {
    val count = timers.size

    val diameter = height
    val radius = diameter / 2f
    val spacing = (width - (4f * diameter)) / 3f
    val pieTotalWidth = count * diameter + (count - 1) * spacing
    val margin = (width - pieTotalWidth) / 2f

    timers.forEachIndexed { i, time ->
      val cx = renderX + margin + radius + i * (2 * radius + spacing)
      val cy = renderY + radius

      val target = (time.toFloat() / totemDuration).coerceIn(0f, 1f)
      val last = displayRatios.getOrDefault(i, target)
      val ratio = lerp(last, target, deltaTracker.gameTimeDeltaTicks)
      displayRatios[i] = ratio

      val red = lerpInt(255, 0, ratio)
      val green = lerpInt(0, 255, ratio)
      val blue = lerpInt(70, 78, ratio)
      val color = (0xFF shl 24) or (red shl 16) or (green shl 8) or blue

      ctx.drawCircle(cx, cy, radius, color, ratio)

      val text = time.toString().padStart(2, '0')
      val tx = (cx - mc.font.width(text) / 2f).toInt()
      val ty = (cy - mc.font.lineHeight / 2f).toInt()
      ctx.drawString(mc.font, text, tx, ty, 0xff_ffffff.toInt())
    }
  }

  private fun lerp(start: Float, end: Float, t: Float) = start + (end - start) * t
  private fun lerpInt(a: Int, b: Int, t: Float) = (a + (b - a) * t).toInt()

  private fun onTotemActivate(event: TotemEvent.Activated) {
    val totem = Models.ShamanTotem.getTotem(event.totemNumber)
    totemDuration = max(totem.time, totemDuration)
  }

  private fun onTotemRemove() {
    if (Models.ShamanTotem.activeTotems.isEmpty()) {
      totemDuration = -1
    }
  }
}
