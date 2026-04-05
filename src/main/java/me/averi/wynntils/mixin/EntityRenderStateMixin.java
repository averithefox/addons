package me.averi.wynntils.mixin;

import me.averi.wynntils.interfaces.EntityFieldAccessor;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(EntityRenderState.class)
public class EntityRenderStateMixin implements EntityFieldAccessor {
  @Unique
  private Entity entity;

  @Override
  public void fox$setEntity(Entity entity) {
    this.entity = entity;
  }

  @Override
  public Entity fox$getEntity() {
    return entity;
  }
}
