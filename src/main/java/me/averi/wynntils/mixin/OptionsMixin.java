package me.averi.wynntils.mixin;

import com.mojang.serialization.Codec;

import java.io.File;

import net.minecraft.client.Minecraft;
import net.minecraft.client.OptionInstance;
import net.minecraft.client.Options;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Options.class)
public class OptionsMixin {
  @Shadow
  @Final
  @Mutable
  private OptionInstance<Integer> fov;

  @Inject(method = "<init>", at = @At("TAIL"))
  private void init(Minecraft minecraft, File file, CallbackInfo ci) {
    fov.values = new OptionInstance.IntRange(2, 160);
  }
}
