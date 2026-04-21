package me.averi.wynntils.events

import com.mojang.blaze3d.vertex.PoseStack
import net.minecraft.client.renderer.SubmitNodeCollector
import net.minecraft.client.renderer.entity.state.EntityRenderState
import net.minecraft.client.renderer.state.CameraRenderState
import net.minecraft.world.entity.Entity
import net.neoforged.bus.api.Event
import net.neoforged.bus.api.ICancellableEvent

/**
 * Published from [net.minecraft.client.renderer.entity.EntityRenderer.submit] for
 */
data class EntityRenderEvent(
  val entity: Entity,
  val matrices: PoseStack,
  val queue: SubmitNodeCollector,
  val cameraState: CameraRenderState,
  val renderState: EntityRenderState,
) : Event(), ICancellableEvent
