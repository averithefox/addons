package me.averi.skyblock.mixin;

import me.averi.skyblock.dungeons.DungeonSecretWaypoints;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.network.protocol.game.ClientboundTakeItemEntityPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPacketListener.class)
public abstract class ClientPacketListenerMixin {
  @Inject(method = "handleTakeItemEntity", at = @At("HEAD"))
  private void foxAddons$handleTakeItemEntity(ClientboundTakeItemEntityPacket packet, CallbackInfo ci) {
    DungeonSecretWaypoints.INSTANCE.handleItemPickup(packet);
  }

  @Inject(method = "handleSoundEvent", at = @At("HEAD"))
  private void foxAddons$handleSoundEvent(ClientboundSoundPacket packet, CallbackInfo ci) {
    DungeonSecretWaypoints.INSTANCE.handleSound(packet);
  }
}
