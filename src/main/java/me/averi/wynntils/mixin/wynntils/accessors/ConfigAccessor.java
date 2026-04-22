package me.averi.wynntils.mixin.wynntils.accessors;

import com.wynntils.core.persisted.config.Config;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value = Config.class, remap = false)
public interface ConfigAccessor {
  @Accessor("userEdited")
  void setUserEdited(boolean userEdited);
}
