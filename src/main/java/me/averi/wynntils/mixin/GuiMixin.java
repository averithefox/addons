package me.averi.wynntils.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import me.averi.wynntils.features.CameraTweaks;
import net.minecraft.client.gui.Gui;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Gui.class)
public class GuiMixin {
  @ModifyExpressionValue(method = "renderCrosshair", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/CameraType;isFirstPerson()Z"))
  private boolean fox$renderCrosshairInThirdPerson(boolean original) {
    return original || CameraTweaks.INSTANCE.getRenderCrosshair();
  }
}
