package me.averi.wynntils.mixin;

import me.averi.wynntils.TotemTimer;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.game.ClientboundRemoveEntitiesPacket;
import net.minecraft.network.protocol.game.ClientboundSetEntityDataPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPacketListener.class)
public class ClientPacketListenerMixin {
  @Inject(method = "handleSetEntityData", at = @At("TAIL"))
  private void handleSetEntityData(ClientboundSetEntityDataPacket packet, CallbackInfo ci) {
    TotemTimer.INSTANCE.onEntityData(packet);
  }

  @Inject(method = "handleRemoveEntities", at = @At("TAIL"))
  private void handleRemoveEntities(ClientboundRemoveEntitiesPacket packet, CallbackInfo ci) {
    TotemTimer.INSTANCE.onRemoveEntities(packet);
  }
}