package me.averi.skyblock

import com.google.gson.GsonBuilder
import com.mojang.blaze3d.platform.InputConstants
import com.mojang.serialization.JsonOps
import me.averi.skyblock.dungeons.DungeonSecretWaypoints
import me.averi.skyblock.mixin.AbstractContainerScreenAccessor
import me.averi.skyblock.mixin.KeyMappingAccessor
import net.fabricmc.api.ClientModInitializer
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper
import net.minecraft.client.KeyMapping
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen
import net.minecraft.resources.RegistryOps
import net.minecraft.world.item.ItemStack
import org.lwjgl.glfw.GLFW

object Main : ClientModInitializer {
  private val copyItemStackKey = KeyMapping(
    "key.foxaddons.copy_itemstack",
    GLFW.GLFW_KEY_F7,
    KeyMapping.Category.INVENTORY,
  )

  private var copyKeyWasDown = false

  override fun onInitializeClient() {
    KeyBindingHelper.registerKeyBinding(copyItemStackKey)
    DungeonSecretWaypoints.init()

    ClientTickEvents.END_CLIENT_TICK.register { client ->
      val down = isBindingKeyDown(client, copyItemStackKey)
      val pressed = down && !copyKeyWasDown
      copyKeyWasDown = down
      if (!pressed) return@register

      val screen = client.screen
      if (screen !is AbstractContainerScreen<*>) return@register
      val accessor = screen as AbstractContainerScreenAccessor<*>
      val slot = accessor.hoveredSlot ?: return@register
      val stack = slot.item
      if (stack.isEmpty) return@register
      val provider = client.level?.registryAccess() ?: client.player?.connection?.registryAccess() ?: return@register
      val ops = RegistryOps.create(JsonOps.INSTANCE, provider)
      val json = ItemStack.CODEC.encodeStart(ops, stack).result().orElse(null) ?: return@register
      val text = GsonBuilder().setPrettyPrinting().create().toJson(json)
      client.keyboardHandler.clipboard = text
    }
  }

  private fun isBindingKeyDown(client: Minecraft, mapping: KeyMapping): Boolean {
    val key = (mapping as KeyMappingAccessor).key
    val window = client.window
    return when (key.type) {
      InputConstants.Type.KEYSYM -> InputConstants.isKeyDown(window, key.value)
      InputConstants.Type.MOUSE -> GLFW.glfwGetMouseButton(window.handle(), key.value) == GLFW.GLFW_PRESS
      InputConstants.Type.SCANCODE -> false
    }
  }
}
