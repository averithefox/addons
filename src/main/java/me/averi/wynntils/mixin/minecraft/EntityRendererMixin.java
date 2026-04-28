package me.averi.wynntils.mixin.minecraft;

import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.vertex.PoseStack;
import me.averi.wynntils.events.EntityRenderEvent;
import me.averi.wynntils.events.EntityShouldRenderEvent;
import me.averi.wynntils.events.EventBus;
import me.averi.wynntils.interfaces.EntityFieldAccessor;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRenderer;

import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EntityRenderer.class)
public class EntityRendererMixin<T extends Entity, S extends EntityRenderState> {
  @Inject(method = "createRenderState(Lnet/minecraft/world/entity/Entity;F)Lnet/minecraft/client/renderer/entity/state/EntityRenderState;", at = @At("RETURN"))
  private void createRenderState(T entity, float f, CallbackInfoReturnable<S> cir, @Local S entityRenderState) {
    ((EntityFieldAccessor) entityRenderState).fox$setEntity(entity);
  }

  @Inject(method = "submit", at = @At("HEAD"), cancellable = true)
  private void render(S entityRenderState, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState cameraRenderState, CallbackInfo ci) {
    var entity = ((EntityFieldAccessor) entityRenderState).fox$getEntity();
    var event = new EntityRenderEvent(entity, poseStack, submitNodeCollector, cameraRenderState, entityRenderState);
    if (EventBus.publish(event)) {
      ci.cancel();
    }
  }

  @Inject(method = "shouldRender", at = @At("HEAD"), cancellable = true)
  private void shouldRender(T entity, Frustum frustum, double x, double y, double z, CallbackInfoReturnable<Boolean> cir) {
    var event = new EntityShouldRenderEvent(entity, frustum, x, y, z);
    if (EventBus.publish(event)) {
      cir.setReturnValue(false);
    }
  }
}
