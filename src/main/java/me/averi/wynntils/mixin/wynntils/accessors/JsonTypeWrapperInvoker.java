package me.averi.wynntils.mixin.wynntils.accessors;

import com.wynntils.core.json.JsonTypeWrapper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.lang.reflect.ParameterizedType;

@Mixin(value = JsonTypeWrapper.class, remap = false)
public interface JsonTypeWrapperInvoker {
  @Invoker("<init>")
  static JsonTypeWrapper invokeConstructor(ParameterizedType type) {
    throw new AssertionError();
  }
}
