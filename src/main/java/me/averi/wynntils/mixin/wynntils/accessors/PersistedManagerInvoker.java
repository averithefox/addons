package me.averi.wynntils.mixin.wynntils.accessors;

import com.wynntils.core.persisted.PersistedManager;
import com.wynntils.core.persisted.PersistedOwner;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(value = PersistedManager.class, remap = false)
public interface PersistedManagerInvoker {
  @Invoker("getPrefix")
  String invokeGetPrefix(PersistedOwner owner);
}
