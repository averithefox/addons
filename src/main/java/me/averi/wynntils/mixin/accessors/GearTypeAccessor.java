package me.averi.wynntils.mixin.accessors;

import com.wynntils.models.gear.type.GearType;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value = GearType.class, remap = false)
public interface GearTypeAccessor {
  @Accessor("modelKey")
  String getModelKey();

  @Accessor("skinModelKey")
  @Nullable String getSkinModelKey();
}
