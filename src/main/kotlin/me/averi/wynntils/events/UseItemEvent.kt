package me.averi.wynntils.events

import net.minecraft.world.InteractionHand
import net.minecraft.world.entity.player.Player

data class UseItemEvent @JvmOverloads constructor(
  val player: Player,
  val hand: InteractionHand,
  override var isCancelled: Boolean = false
) : CancellableEvent
