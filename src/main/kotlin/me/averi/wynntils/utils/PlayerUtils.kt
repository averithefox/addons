package me.averi.wynntils.utils

import net.minecraft.client.player.LocalPlayer
import net.minecraft.network.protocol.game.ServerboundUseItemPacket
import net.minecraft.world.InteractionHand
import net.minecraft.world.InteractionResult

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
