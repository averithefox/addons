package me.averi.wynntils.mixin.wynntils;

import com.wynntils.features.debug.PacketDebuggerFeature;
import com.wynntils.mc.event.PacketEvent;
import me.averi.wynntils.features.PacketDebugger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = PacketDebuggerFeature.class, remap = false)
public class PacketDebuggerFeatureMixin {
  @Inject(method = "onPacketSent", at = @At("HEAD"), cancellable = true)
  private void onPacketSent(PacketEvent.PacketSentEvent<?> e, CallbackInfo ci) {
    if (PacketDebugger.INSTANCE.getPacketDirection() == PacketDebugger.PacketDirection.CLIENTBOUND) ci.cancel();
  }

  @Inject(method = "onPacketReceived", at = @At("HEAD"), cancellable = true)
  private void onPacketReceived(PacketEvent.PacketReceivedEvent<?> e, CallbackInfo ci) {
    if (PacketDebugger.INSTANCE.getPacketDirection() == PacketDebugger.PacketDirection.SERVERBOUND) ci.cancel();
  }
}
