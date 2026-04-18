package me.averi.wynntils.mixin;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.llamalad7.mixinextras.sugar.Local;
import com.wynntils.core.consumers.features.Feature;
import com.wynntils.core.consumers.features.FeatureManager;
import com.wynntils.core.persisted.config.Category;
import com.wynntils.core.persisted.config.ConfigCategory;
import me.averi.wynntils.features.CameraTweaks;
import me.averi.wynntils.features.Debug;
import me.averi.wynntils.features.ShamanTotemUtils;
import me.averi.wynntils.features.SkinChanger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = FeatureManager.class, remap = false)
public abstract class FeatureManagerMixin {
  @Shadow
  protected abstract void registerFeature(Feature feature);

  @Inject(method = "init", at = @At(value = "INVOKE", target = "Lcom/wynntils/core/consumers/features/FeatureCommands;init()V"))
  private void init(CallbackInfo ci) {
    registerFeature(ShamanTotemUtils.INSTANCE);
    registerFeature(Debug.INSTANCE);
    registerFeature(CameraTweaks.INSTANCE);
    registerFeature(SkinChanger.INSTANCE);
  }

  @WrapWithCondition(method = "initializeFeature", at = @At(value = "INVOKE", target = "Lcom/wynntils/core/consumers/features/Feature;setCategory(Lcom/wynntils/core/persisted/config/Category;)V"))
  private boolean fox$dontOverrideCategory(Feature instance, Category category, @Local(name = "configCategory") ConfigCategory configCategory) {
    return configCategory != null;
  }
}
