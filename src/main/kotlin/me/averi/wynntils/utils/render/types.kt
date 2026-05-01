package me.averi.wynntils.utils.render

import com.mojang.blaze3d.pipeline.RenderPipeline
import com.mojang.blaze3d.platform.DepthTestFunction
import com.mojang.blaze3d.vertex.DefaultVertexFormat
import com.mojang.blaze3d.vertex.VertexFormat
import net.minecraft.client.renderer.RenderPipelines
import net.minecraft.client.renderer.rendertype.RenderSetup
import net.minecraft.client.renderer.rendertype.RenderType

private fun renderPipeline(vararg snippets: RenderPipeline.Snippet, block: RenderPipeline.Builder.() -> Unit) =
  RenderPipelines.register(RenderPipeline.builder(*snippets).apply(block).build())

val LINES_THROUGH_WALLS by lazy {
  RenderType.create(
    "lines_through_walls",
    RenderSetup.builder(
      renderPipeline(RenderPipelines.LINES_SNIPPET) {
        withLocation("lines_through_walls")
        withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
      }
    ).createRenderSetup()
  )
}

val FILLED_TRIANGLES_THROUGH_WALLS by lazy {
  RenderType.create(
    "filled_triangles_through_walls",
    RenderSetup.builder(
      renderPipeline(RenderPipelines.DEBUG_FILLED_SNIPPET) {
        withLocation("filled_triangles_through_walls")
        withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
        withVertexFormat(DefaultVertexFormat.POSITION_COLOR, VertexFormat.Mode.TRIANGLES)
      }
    ).createRenderSetup()
  )
}

val FILLED_TRIANGLE_FAN_THROUGH_WALLS by lazy {
  RenderType.create(
    "filled_triangle_fan_through_walls", RenderSetup.builder(
      renderPipeline(RenderPipelines.DEBUG_FILLED_SNIPPET) {
        withLocation("filled_triangle_fan_through_walls")
        withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
        withVertexFormat(DefaultVertexFormat.POSITION_COLOR, VertexFormat.Mode.TRIANGLE_FAN)
      }).createRenderSetup()
  )
}

val FILLED_TRIANGLE_FAN by lazy {
  RenderType.create(
    "filled_triangle_fan_through_walls", RenderSetup.builder(
      renderPipeline(RenderPipelines.DEBUG_FILLED_SNIPPET) {
        withLocation("filled_triangle_fan_through_walls")
        withVertexFormat(DefaultVertexFormat.POSITION_COLOR, VertexFormat.Mode.TRIANGLE_FAN)
      }).createRenderSetup()
  )
}
