package me.averi.wynntils.utils

import me.averi.wynntils.constants.WynnClass
import net.minecraft.client.player.LocalPlayer
import net.minecraft.network.protocol.game.ServerboundUseItemPacket
import net.minecraft.world.InteractionHand
import net.minecraft.world.InteractionResult
import net.minecraft.world.item.Item
import net.minecraft.world.item.TooltipFlag

fun LocalPlayer.attack() {
  resetAttackStrengthTicker()
  swing(InteractionHand.MAIN_HAND)
}

fun LocalPlayer.use() {
  val hand = InteractionHand.MAIN_HAND
  connection.send(ServerboundUseItemPacket(hand, 0, yRot, xRot))
  val result = getItemInHand(hand).use(level(), this, hand)

  if (result is InteractionResult.Success && result.swingSource() == InteractionResult.SwingSource.CLIENT) {
    swing(hand, false)
  }

  if (result.consumesAction()) {
    mc.gameRenderer.itemInHandRenderer.itemUsed(hand)
  }
}

fun LocalPlayer.getWynnClass() = inventory.items.let { items ->
  for (item in items) {
    val tooltip = item.getTooltipLines(Item.TooltipContext.EMPTY, this, TooltipFlag.NORMAL)
    val reqLine = tooltip.find { line -> line.string.startsWith("\uE006\uDAFF\uDFFF Class Type\uDAFF\uDFC4\uDB00\uDC57") } ?: continue
    return@let WynnClass.entries.find { wynnClass -> reqLine.string.endsWith(wynnClass.reqString) } ?: continue
  }
  null
}
