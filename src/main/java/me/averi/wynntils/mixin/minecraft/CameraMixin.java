package me.averi.wynntils.mixin.minecraft;

import me.averi.wynntils.features.CameraTweaks;
import net.minecraft.client.Camera;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Camera.class)
public class CameraMixin {
  @Inject(method = "getMaxZoom", at = @At("HEAD"), cancellable = true)
  private void getMaxZoom(float f, CallbackInfoReturnable<Float> cir) {
    if (CameraTweaks.INSTANCE.getNoCameraClip()) cir.setReturnValue(f);
  }
}
