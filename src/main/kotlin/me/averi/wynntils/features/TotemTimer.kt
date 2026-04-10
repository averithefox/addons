package me.averi.wynntils.features

import me.averi.wynntils.events.EntityDataEvent
import me.averi.wynntils.events.EventBus.subscribe
import me.averi.wynntils.events.RemoveEntitiesEvent
import me.averi.wynntils.utils.drawCircle
import me.averi.wynntils.utils.mc
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientWorldEvents
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElement
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry
import net.minecraft.client.DeltaTracker
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.multiplayer.ClientLevel
import net.minecraft.network.protocol.game.ClientboundRemoveEntitiesPacket
import net.minecraft.network.protocol.game.ClientboundSetEntityDataPacket
import net.minecraft.resources.Identifier
import net.minecraft.world.entity.Display
import java.util.regex.Pattern
import kotlin.math.max

object TotemTimer : HudElement, ClientWorldEvents.AfterClientWorldChange {
  private val TOTEM =
    Pattern.compile("(?<owner>.+)'s? Totem\n(\\+(?<hpr>\\d+)❤/s )?(\uE013 (\\d+)s )?\uE01F (?<time>\\d+)s")

  private var totemTime = -1
  private val totems = mutableMapOf<Int, Int>()

  private val displayRatios = mutableMapOf<Int, Float>()
  private fun lerp(start: Float, end: Float, t: Float) = start + (end - start) * t

  fun register() {
    ClientWorldEvents.AFTER_CLIENT_WORLD_CHANGE.register(::afterWorldChange)

    HudElementRegistry.addLast(
      Identifier.fromNamespaceAndPath("foxaddons", "totem_timer"), this
    )

    subscribe<EntityDataEvent> {
      onEntityData(packet)
    }

    subscribe<RemoveEntitiesEvent> {
      onRemoveEntities(packet)
    }
  }

  override fun render(ctx: GuiGraphics, deltaTracker: DeltaTracker) {
    val dt = deltaTracker.gameTimeDeltaTicks

    val r = 10f
    val pad = 5f
    val spacing = 2 * r + pad
    val count = totems.size
    val totalW = count * 2 * r + (count - 1) * pad
    val startX = (ctx.guiWidth() - totalW) / 2f
    val y = ctx.guiHeight() - 100

    totems.values.forEachIndexed { i, timeLeft ->
      val target = (timeLeft.toFloat() / totemTime).coerceIn(0f, 1f)
      val last = displayRatios.getOrDefault(i, target)
      val smooth = lerp(last, target, dt)
      displayRatios[i] = smooth
      fun lerpInt(a: Int, b: Int, t: Float) = (a + (b - a) * t).toInt()

      val red = lerpInt(255, 0, smooth)
      val green = lerpInt(0, 255, smooth)
      val blue = lerpInt(70, 78, smooth)
      val color = (0xFF shl 24) or (red shl 16) or (green shl 8) or blue

      val cx = startX + i * spacing + r
      val cy = y + mc.font.lineHeight / 2f
      ctx.drawCircle(cx, cy, r, color, smooth)

      val text = timeLeft.toString().padStart(2, '0')
      val tx = (cx - mc.font.width(text) / 2f).toInt()
      ctx.drawString(mc.font, text, tx, y, 0xff_ffffff.toInt())
    }
  }

  fun onEntityData(packet: ClientboundSetEntityDataPacket) {
    val entity = mc.level?.getEntity(packet.id) ?: return
    if (entity !is Display.TextDisplay) return

    val totemMatcher = TOTEM.matcher(entity.text.string)
    if (totemMatcher.matches()) {
      if (totemMatcher.group("owner") != mc.gameProfile.name) return
      val time = totemMatcher.group("time").toInt()
      totems[packet.id] = time
      totemTime = max(time, totemTime)
      return
    }
  }

  fun onRemoveEntities(packet: ClientboundRemoveEntitiesPacket) {
    packet.entityIds.forEach(totems::remove)
    if (totems.isEmpty()) totemTime = -1
  }

  override fun afterWorldChange(mc: Minecraft, level: ClientLevel) {
    totems.clear()
    totemTime = -1
  }
}