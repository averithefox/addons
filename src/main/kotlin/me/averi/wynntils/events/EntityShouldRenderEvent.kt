package me.averi.wynntils.events

import net.minecraft.client.renderer.culling.Frustum
import net.minecraft.world.entity.Entity
import net.neoforged.bus.api.Event
import net.neoforged.bus.api.ICancellableEvent

/**
 * Published from [net.minecraft.client.renderer.entity.EntityRenderer.shouldRender]
 */
data class EntityShouldRenderEvent(
  val entity: Entity,
  val frustum: Frustum,
  val x: Double,
  val y: Double,
  val z: Double
) : Event(), ICancellableEvent
