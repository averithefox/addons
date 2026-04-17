package me.averi.wynntils.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import com.wynntils.core.persisted.PersistedManager;
import com.wynntils.core.persisted.PersistedOwner;
import com.wynntils.core.persisted.PersistedValue;
import com.wynntils.core.persisted.config.Config;
import com.wynntils.core.persisted.config.ConfigManager;
import com.wynntils.core.persisted.type.PersistedMetadata;
import me.averi.wynntils.dx.Configurable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.lang.reflect.Type;
import java.util.List;

@Mixin(value = ConfigManager.class, remap = false)
public class ConfigManagerMixin {
  @Redirect(method = "registerConfigOptions", at = @At(value = "INVOKE", target = "Lcom/wynntils/core/persisted/PersistedManager;getMetadata(Lcom/wynntils/core/persisted/PersistedValue;)Lcom/wynntils/core/persisted/type/PersistedMetadata;"))
  private <T> PersistedMetadata<T> fox$preventGetMetadataFromRunning(PersistedManager instance, PersistedValue<T> persisted) {
    return null;
  }

  @Redirect(method = "registerConfigOptions", at = @At(value = "INVOKE", target = "Lcom/wynntils/core/persisted/type/PersistedMetadata;valueType()Ljava/lang/reflect/Type;"))
  private Type fox$useConfigsGetType(PersistedMetadata<?> instance, @Local(name = "config") Config<?> config) {
    return config.getType();
  }

  @ModifyReturnValue(method = "getConfigOptions", at = @At("RETURN"))
  private List<Config<?>> fox$appendSettings(List<Config<?>> original, @Local(argsOnly = true, name = "owner") PersistedOwner owner) {
    if (owner instanceof Configurable configurable) {
      original.addAll(configurable.getSettings());
    }
    return original;
  }
}
