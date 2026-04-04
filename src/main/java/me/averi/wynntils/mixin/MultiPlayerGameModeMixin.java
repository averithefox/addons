package me.averi.wynntils.mixin;

import me.averi.wynntils.ClickQueue;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MultiPlayerGameMode.class)
public class MultiPlayerGameModeMixin {
  @Inject(method = "useItem", at = @At("HEAD"), cancellable = true)
  private void useItem(Player player, InteractionHand interactionHand, CallbackInfoReturnable<InteractionResult> cir) {
    if (!ClickQueue.INSTANCE.isEmpty()) cir.setReturnValue(InteractionResult.FAIL);
  }
}
