package me.averi.wynntils.widgets

import com.wynntils.utils.mc.McUtils
import me.averi.wynntils.dx.ItemModelSetting
import me.averi.wynntils.screens.ItemModelSelectorScreen
import me.averi.wynntils.utils.itemStackWithModel
import me.averi.wynntils.utils.renderItem
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.components.AbstractWidget
import net.minecraft.client.gui.narration.NarrationElementOutput
import net.minecraft.client.input.MouseButtonEvent
import net.minecraft.network.chat.Component
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items

class ItemModelSettingWidget(x: Int, y: Int, val setting: ItemModelSetting) :
  AbstractWidget(x, y, 20, 20, Component.empty()) {
  override fun renderWidget(
    ctx: GuiGraphics, mouseX: Int, mouseY: Int, partialTicks: Float
  ) {
    val model = setting.modelValue
    val centerX = x + width / 2f
    val centerY = y + height / 2f

    val itemStack = if (model != null) itemStackWithModel(model) else ItemStack(Items.BARRIER)
    ctx.renderItem(itemStack, centerX, centerY, 1f)
  }

  override fun updateWidgetNarration(narrationElementOutput: NarrationElementOutput) {}

  override fun onClick(event: MouseButtonEvent, doubled: Boolean) {
    if (event.isRight) return
    McUtils.setScreen(ItemModelSelectorScreen(McUtils.screen(), setting))
  }
}