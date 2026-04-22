package me.averi.wynntils.mixin.wynntils;

import com.wynntils.core.persisted.config.Category;
import com.wynntils.utils.render.Texture;
import net.minecraft.client.resources.language.I18n;
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

@Mixin(value = Category.class, remap = false)
public abstract class CategoryMixin {
  @Shadow
  @Final
  @Mutable
  private static Category[] $VALUES;

  @Unique
  private static Category FOX_ADDONS;

  @Invoker("<init>")
  private static Category invokeConstructor(String internalName, int internalId, Texture categoryIcon) {
    throw new AssertionError();
  }

  @Inject(method = "<clinit>", at = @At("TAIL"))
  private static void fox$appendCategory(CallbackInfo ci) {
    Category[] prior = $VALUES;
    int nextOrdinal = prior.length;
    FOX_ADDONS = invokeConstructor("FOX_ADDONS", nextOrdinal, Texture.valueOf("FOX_ADDONS_CONFIG_ICON"));
    Category[] neu = Arrays.copyOf(prior, prior.length + 1);
    neu[nextOrdinal] = FOX_ADDONS;
    $VALUES = neu;
  }

  @Inject(method = "toString", at = @At("HEAD"), cancellable = true)
  private void fox$toString(CallbackInfoReturnable<String> cir) {
    if (((Category) (Object) this) == FOX_ADDONS) {
      cir.setReturnValue(I18n.get("category.foxaddons.main"));
    }
  }
}
