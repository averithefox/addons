package me.averi.wynntils.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.vertex.PoseStack;
import me.averi.wynntils.DebugRenderer;
import me.averi.wynntils.interfaces.EntityFieldAccessor;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;

import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.Display;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.Items;
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

  @Inject(method = "submit", at = @At("HEAD"))
  private void render(S entityRenderState, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState cameraRenderState, CallbackInfo ci) {
    var entity = ((EntityFieldAccessor) entityRenderState).fox$getEntity();
    if (entity instanceof Display.ItemDisplay itemDisplay && itemDisplay.getItemStack().is(Items.OAK_BOAT)) {
      var modelComponent = itemDisplay.getItemStack().get(DataComponents.CUSTOM_MODEL_DATA);
      if (modelComponent != null) {
        var model = modelComponent.getFloat(0);
        if (model != null && model == 30601f) {
          float scale = 0.4f;
          float offset = 1f;
          poseStack.translate(0.0, -offset * (1.0f - scale), 0.0);
          poseStack.scale(scale, scale, scale);
        }
      }
    }

    DebugRenderer.INSTANCE.onRenderEntity(entity, poseStack, submitNodeCollector, cameraRenderState, entityRenderState.lightCoords);
  }
}
