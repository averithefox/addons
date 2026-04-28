package me.averi.wynntils.utils.render

import com.mojang.blaze3d.vertex.PoseStack
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderContext
import net.minecraft.client.renderer.MultiBufferSource
import net.minecraft.client.renderer.SubmitNodeCollector

data class RenderContext(val matrices: PoseStack, val consumers: MultiBufferSource, val queue: SubmitNodeCollector)

val WorldRenderContext.asRenderCtx get() = RenderContext(matrices(), consumers(), commandQueue())
