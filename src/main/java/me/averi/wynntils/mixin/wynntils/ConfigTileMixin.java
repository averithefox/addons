package me.averi.wynntils.mixin.wynntils;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import com.wynntils.core.persisted.config.Config;
import com.wynntils.screens.settings.widgets.ConfigTile;
import me.averi.wynntils.dx.ItemModelSetting;
import me.averi.wynntils.widgets.ItemModelSettingWidget;
import net.minecraft.client.gui.components.AbstractWidget;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = ConfigTile.class, remap = false)
public abstract class ConfigTileMixin {
  @Shadow
  protected abstract int getRenderX();

  @Shadow
  protected abstract int getRenderY();

  @ModifyExpressionValue(method = "getWidgetFromConfig", at = @At(value = "INVOKE", target = "Ljava/lang/reflect/Type;equals(Ljava/lang/Object;)Z", ordinal = 4))
  private boolean fox$acceptPrimitiveTypes(boolean original, @Local(argsOnly = true, name = "configOption") Config<?> configOption) {
    return original || configOption.getType().equals(Boolean.TYPE);
  }

  @Inject(method = "getWidgetFromConfig", at = @At("HEAD"), cancellable = true)
  private void getWidgetFromConfig(Config<?> configOption, CallbackInfoReturnable<AbstractWidget> cir) {
    if (configOption instanceof ItemModelSetting itemModelSetting) {
      cir.setReturnValue(new ItemModelSettingWidget(getRenderX(), getRenderY(), itemModelSetting));
    }
  }
}
