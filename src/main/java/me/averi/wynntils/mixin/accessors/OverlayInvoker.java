package me.averi.wynntils.mixin.accessors;

import com.wynntils.core.consumers.overlays.Overlay;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(value = Overlay.class, remap = false)
public interface OverlayInvoker {
  @Invoker("getTranslationFeatureKeyName")
  String invokeGetTranslationFeatureKeyName();
}
