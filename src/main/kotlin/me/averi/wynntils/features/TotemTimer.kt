package me.averi.wynntils.features

import com.wynntils.core.components.Models
import com.wynntils.models.abilities.event.TotemEvent
import me.averi.wynntils.events.EventBus.subscribe
import me.averi.wynntils.utils.drawCircle
import me.averi.wynntils.utils.mc
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElement
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements
import net.minecraft.client.DeltaTracker
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.resources.Identifier
import kotlin.math.max

object TotemTimer : HudElement {
  private var totemTime = -1

  private val displayRatios = mutableMapOf<Int, Float>()
  private fun lerp(start: Float, end: Float, t: Float) = start + (end - start) * t

  fun register() {
    HudElementRegistry.attachElementAfter(
      VanillaHudElements.HOTBAR, Identifier.fromNamespaceAndPath("foxaddons", "totem_timer"), this
    )

    subscribe(::onTotemActivate)
    subscribe<TotemEvent.Removed> { onTotemRemove() }
  }

  override fun render(ctx: GuiGraphics, deltaTracker: DeltaTracker) {
    val totems = Models.ShamanTotem.activeTotems.filter { it.time != -1 }

    val r = 10f
    val pad = 5f
    val spacing = 2 * r + pad
    val count = totems.size
    val totalW = count * 2 * r + (count - 1) * pad
    val startX = (ctx.guiWidth() - totalW) / 2f
    val y = ctx.guiHeight() - 100

    totems.forEachIndexed { i, totem ->
      val target = (totem.time.toFloat() / totemTime).coerceIn(0f, 1f)
      val last = displayRatios.getOrDefault(i, target)
      val smooth = lerp(last, target, deltaTracker.gameTimeDeltaTicks)
      displayRatios[i] = smooth
      fun lerpInt(a: Int, b: Int, t: Float) = (a + (b - a) * t).toInt()

      val red = lerpInt(255, 0, smooth)
      val green = lerpInt(0, 255, smooth)
      val blue = lerpInt(70, 78, smooth)
      val color = (0xFF shl 24) or (red shl 16) or (green shl 8) or blue

      val cx = startX + i * spacing + r
      val cy = y + mc.font.lineHeight / 2f
      ctx.drawCircle(cx, cy, r, color, smooth)

      val text = totem.time.toString().padStart(2, '0')
      val tx = (cx - mc.font.width(text) / 2f).toInt()
      ctx.drawString(mc.font, text, tx, y, 0xff_ffffff.toInt())
    }
  }

  fun onTotemActivate(event: TotemEvent.Activated) {
    val totem = Models.ShamanTotem.getTotem(event.totemNumber)
    totemTime = max(totem.time, totemTime)
  }

  fun onTotemRemove() {
    if (Models.ShamanTotem.activeTotems.isEmpty()) {
      totemTime = -1
    }
  }
}
