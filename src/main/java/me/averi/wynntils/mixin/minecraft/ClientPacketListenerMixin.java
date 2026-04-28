package me.averi.wynntils.mixin.minecraft;

import com.llamalad7.mixinextras.sugar.Local;
import me.averi.wynntils.events.EntityDataEvent;
import me.averi.wynntils.events.EventBus;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.game.*;
import net.minecraft.world.entity.Display;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static me.averi.wynntils.utils.ExtKt.getCustomModel;

@Mixin(ClientPacketListener.class)
public class ClientPacketListenerMixin {
  @Shadow
  private ClientLevel level;

  @Inject(method = "handleTeleportEntity", at = @At("TAIL"))
  private void handleTeleportEntity(ClientboundTeleportEntityPacket packet, CallbackInfo ci) {
    var entity = level.getEntity(packet.id());
    if (entity instanceof Display.ItemDisplay itemDisplay) {
      var model = getCustomModel(itemDisplay.getItemStack());
      if (model != null && model == 8592f) {
        System.out.printf("handleTeleportEntity %s\n", packet.change());
      }
    }
  }

  @Inject(method = "handleSetEntityMotion", at = @At("HEAD"))
  private void handleSetEntityMotion(ClientboundSetEntityMotionPacket packet, CallbackInfo ci) {
    var entity = level.getEntity(packet.getId());
  }

  @Inject(method = "handleAddEntity", at = @At("TAIL"))
  private void handleAddEntity(ClientboundAddEntityPacket packet, CallbackInfo ci, @Local Entity entity) {
//    System.out.printf("addEntity %s\n", entity);
//    if (entity instanceof Display.ItemDisplay itemDisplay) {
//      System.out.println(itemDisplay.getItemStack());
//    }
  }

  @Inject(method = "handleSetEntityData", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/syncher/SynchedEntityData;assignValues(Ljava/util/List;)V"))
  private void fox$beforeEntityDataAssignment(ClientboundSetEntityDataPacket packet, CallbackInfo ci, @Local Entity entity) {
    var event = new EntityDataEvent.Pre(entity, packet.packedItems());
    EventBus.publish(event);
  }

  @Inject(method = "handleEntityPositionSync", at = @At("TAIL"))
  private void handleEntityPositionSync(ClientboundEntityPositionSyncPacket packet, CallbackInfo ci) {
    var entity = level.getEntity(packet.id());
    if (entity instanceof Display.ItemDisplay itemDisplay) {
      var model = getCustomModel(itemDisplay.getItemStack());
      if (model != null && model == 8592f) {
        System.out.printf("handleEntityPositionSync %s\n", packet.values());
      }
    }
  }

  @Inject(method = "handleMoveEntity", at = @At("TAIL"))
  private void handleMoveEntity(ClientboundMoveEntityPacket packet, CallbackInfo ci, @Local Entity entity) {
    if (entity instanceof Display.ItemDisplay itemDisplay) {
      var model = getCustomModel(itemDisplay.getItemStack());
      if (model != null && model == 8592f) {
        System.out.printf("handleMoveEntity %d, %d, %d\n", packet.getXa(), packet.getYa(), packet.getZa());
      }
    }
  }
}
