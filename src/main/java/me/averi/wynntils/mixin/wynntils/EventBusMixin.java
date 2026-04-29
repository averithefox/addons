package me.averi.wynntils.mixin.wynntils;

import net.neoforged.bus.EventBus;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@SuppressWarnings("UnstableApiUsage")
@Mixin(value = EventBus.class, remap = false)
public class EventBusMixin {
  @Redirect(method = "register(Ljava/lang/Object;)V", at = @At(value = "INVOKE", target = "Ljava/lang/Class;getDeclaredMethods()[Ljava/lang/reflect/Method;"))
  private Method[] fox$registerSupertypeMethods(Class<?> clazz) {
    var methods = new ArrayList<Method>();
    collectMethods(clazz, methods);
    return methods.toArray(new Method[0]);
  }

  @Inject(method = "checkSupertypes", at = @At("HEAD"), cancellable = true)
  private static void checkSupertypes(Class<?> registeredType, Class<?> type, CallbackInfo ci) {
    ci.cancel();
  }

  @Unique
  private void collectMethods(Class<?> clazz, List<Method> methods) {
    if (clazz == null || clazz == Object.class) {
      return;
    }

    methods.addAll(Arrays.asList(clazz.getDeclaredMethods()));

    collectMethods(clazz.getSuperclass(), methods);

    for (Class<?> iface : clazz.getInterfaces()) {
      collectMethods(iface, methods);
    }
  }
}
