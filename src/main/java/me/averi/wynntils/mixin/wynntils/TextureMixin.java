package me.averi.wynntils.mixin.wynntils;

import com.wynntils.utils.render.Texture;
import net.minecraft.resources.Identifier;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.gen.Invoker;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Arrays;

@Mixin(value = Texture.class, remap = false)
public class TextureMixin {
  @Shadow
  @Final
  @Mutable
  private static Texture[] $VALUES;

  @Unique
  private static Texture FOX_ADDONS_CONFIG_ICON;

  @Invoker("<init>")
  private static Texture invokeConstructor(String internalName, int internalId, String name, int width, int height) {
    throw new AssertionError();
  }

  @Inject(method = "<clinit>", at = @At("TAIL"))
  private static void fox$appendTexture(CallbackInfo ci) {
    Texture[] prior = $VALUES;
    int nextOrdinal = prior.length;
    FOX_ADDONS_CONFIG_ICON = invokeConstructor("FOX_ADDONS_CONFIG_ICON", nextOrdinal, "", 16, 16);
    Texture[] neu = Arrays.copyOf(prior, prior.length + 1);
    neu[nextOrdinal] = FOX_ADDONS_CONFIG_ICON;
    $VALUES = neu;
  }

  @Inject(method = "identifier", at = @At("HEAD"), cancellable = true)
  private void fox$identifier(CallbackInfoReturnable<Identifier> cir) {
    if ((Object) this == FOX_ADDONS_CONFIG_ICON) {
      cir.setReturnValue(Identifier.fromNamespaceAndPath("foxaddons", "textures/icons/config_categories/fox_addons_config_icon.png"));
    }
  }
}
