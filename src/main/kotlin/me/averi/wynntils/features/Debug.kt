package me.averi.wynntils.features

import com.wynntils.core.consumers.features.ProfileDefault
import me.averi.wynntils.dx.Feature
import me.averi.wynntils.events.EntityDataEvent
import me.averi.wynntils.events.EntityRenderEvent
import me.averi.wynntils.events.EntityShouldRenderEvent
import me.averi.wynntils.events.EventBus.subscribe
import me.averi.wynntils.utils.cancel
import me.averi.wynntils.utils.customModel
import me.averi.wynntils.utils.mc
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderContext
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderEvents
import net.minecraft.client.gui.Font
import net.minecraft.network.chat.Component
import net.minecraft.network.syncher.EntityDataAccessor
import net.minecraft.network.syncher.SynchedEntityData
import net.minecraft.world.entity.Display
import net.minecraft.world.entity.Pose
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items

object Debug : Feature(ProfileDefault.DISABLED) {
  init {
    subscribe(::onEntityRender)
    subscribe(::preEntityData)
    WorldRenderEvents.AFTER_ENTITIES.register(::onWorldRender)
    subscribe(::onEntityShouldRender)
  }

  private fun onEntityRender(e: EntityRenderEvent) {
    if (!isEnabled) return
    if (e.entity !is Display.ItemDisplay) return

    val models = e.entity.level().getEntitiesOfClass(Display.ItemDisplay::class.java, e.entity.boundingBox.inflate(.1))
      .mapNotNull { it.itemStack.customModel?.toInt() }.sorted().distinct()
    val (first, last) = models.first() to models.last()
    val modelText = when {
      first == last -> "$first"
      last == first + models.size - 1 -> "$first..$last"
      else -> models.joinToString(",")
    }
    val text = e.entity.itemStack.run {
      val itemStr = if (item == Items.OAK_BOAT) "" else "${item.toString().replace("minecraft:", "")} "
      "$itemStr$modelText"
    }

    e.matrices.pushPose()
    e.matrices.translate(0f, mc.player!!.eyeHeight, 0f)
    e.matrices.mulPose(e.cameraState.orientation)
    e.matrices.scale(0.015f, -0.015f, 0.015f)

    e.queue.submitText(
      e.matrices,
      48f,
      0f,
      Component.literal(text).visualOrderText,
      false,
      Font.DisplayMode.SEE_THROUGH,
      e.renderState.lightCoords,
      -1,
      (mc.options.getBackgroundOpacity(0.25f) * 255.0f).toInt() shl 24,
      0xFF_000000.toInt()
    )

    e.matrices.popPose()
  }

  private fun onWorldRender(ctx: WorldRenderContext) {
//    val color = Color(1f, 0f, 0f, .5f)
//
//    val playerPos = mc.player?.position() ?: return
//
//    val p1 = playerPos.toVector3f()
//    val p2 = playerPos.add(2.0, 0.0, 2.0).toVector3f()
//    val p3 = playerPos.add(-2.0, 0.0, 2.0).toVector3f()
//
//    ctx.asRenderCtx.withWorldTranslation { matrix ->
//      val buffer = consumers.getBuffer(FILLED_THROUGH_WALLS)
//
//      buffer.addVertex(matrix, p1).setColor(color)
//      buffer.addVertex(matrix, p3).setColor(color)
//      buffer.addVertex(matrix, p2).setColor(color)
//
//      buffer.addVertex(matrix, p1).setColor(color)
//      buffer.addVertex(matrix, p2).setColor(color)
//      buffer.addVertex(matrix, p3).setColor(color)
//    }
  }

  private fun preEntityData(e: EntityDataEvent.Pre) {
//    if (!isEnabled) return
    if (e.entity !is Display.ItemDisplay) return

    e.packedItems.firstItemOfType(Display.ItemDisplay.DATA_ITEM_STACK_ID)?.also { itemStackData ->
      val itemStack = e.entity.itemStack
      if (ItemStack.matches(itemStack, itemStackData)) return@also
      println("${itemStack.item}(${itemStack.customModel}) -> ${itemStackData.item}(${itemStackData.customModel})")
    }

    e.packedItems.firstItemOfType(Display.DATA_TRANSLATION_ID)?.also { translationData ->
      if (e.entity.itemStack.customModel != 8592f) return@also
      println("translation x=${translationData.x()}, y=${translationData.y()}, z=${translationData.z()}")
    }
  }

  private fun onEntityShouldRender(e: EntityShouldRenderEvent) {
//    e.cancel()
    if (e.entity !is Display.ItemDisplay) return
    val model = e.entity.itemStack.customModel ?: return
    if (/* totem AoE circle */model in 30603f..30604f || /* totem particle thingies */model in 30607f..30610f || /* racoon */model in 21312f..21320f || /* fox */model in 1495f..1501f) {
      e.cancel()
    }
  }

  @Suppress("unchecked_cast")
  private fun <T : Any> List<SynchedEntityData.DataValue<*>>.firstItemOfType(accessor: EntityDataAccessor<T>): T? =
    find { it.id == accessor.id }?.value as T?
}
