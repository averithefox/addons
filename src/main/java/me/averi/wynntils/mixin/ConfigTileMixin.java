package me.averi.wynntils.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import com.wynntils.core.persisted.config.Config;
import com.wynntils.screens.settings.widgets.ConfigTile;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value = ConfigTile.class, remap = false)
public class ConfigTileMixin {
  @ModifyExpressionValue(method = "getWidgetFromConfig", at = @At(value = "INVOKE", target = "Ljava/lang/reflect/Type;equals(Ljava/lang/Object;)Z", ordinal = 4))
  private boolean fox$acceptPrimitiveTypes(boolean original, @Local(argsOnly = true, name = "configOption") Config<?> configOption) {
    return original || configOption.getType().equals(Boolean.TYPE);
  }
}
