package me.averi.wynntils.mixin.wynntils;

import com.wynntils.utils.mc.McUtils;
import com.wynntils.utils.mc.MouseUtils;
import me.averi.wynntils.features.QuickCast;
import net.minecraft.world.InteractionHand;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = MouseUtils.class, remap = false)
public class MouseUtilsMixin {
  @Inject(method = "sendLeftClickInput", at = @At("TAIL"))
  private static void sendLeftClickInput(CallbackInfo ci) {
    if (QuickCast.INSTANCE.getSwing()) {
      var player = McUtils.player();
      player.resetAttackStrengthTicker();
      player.swing(InteractionHand.MAIN_HAND, false);
    }
  }
}
