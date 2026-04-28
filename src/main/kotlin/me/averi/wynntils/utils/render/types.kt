package me.averi.wynntils.utils.render

import com.mojang.blaze3d.pipeline.RenderPipeline
import com.mojang.blaze3d.platform.DepthTestFunction
import com.mojang.blaze3d.vertex.DefaultVertexFormat
import com.mojang.blaze3d.vertex.VertexFormat
import net.minecraft.client.renderer.RenderPipelines
import net.minecraft.client.renderer.rendertype.RenderSetup
import net.minecraft.client.renderer.rendertype.RenderType

val LINES_THROUGH_WALLS by lazy {
  RenderType.create(
    "lines_through_walls",
    RenderSetup.builder(
      RenderPipelines.register(
        RenderPipeline.builder(RenderPipelines.LINES_SNIPPET)
          .withLocation("lines_through_walls")
          .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
          .build()
      )
    ).createRenderSetup()
  )
}

val FILLED_THROUGH_WALLS by lazy {
  RenderType.create(
    "filled_through_walls",
    RenderSetup.builder(
      RenderPipelines.register(
        RenderPipeline.builder(RenderPipelines.DEBUG_FILLED_SNIPPET)
          .withLocation("filled_through_walls")
          .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
          .withVertexFormat(DefaultVertexFormat.POSITION_COLOR, VertexFormat.Mode.TRIANGLES)
          .build()
      )
    ).createRenderSetup()
  )
}
