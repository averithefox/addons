package me.averi.wynntils.utils

import net.minecraft.core.component.DataComponents
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import net.minecraft.world.item.component.CustomModelData

fun itemStackWithModel(model: Float): ItemStack {
  val itemStack = ItemStack(Items.POTION)
  itemStack.set(DataComponents.CUSTOM_MODEL_DATA, CustomModelData(listOf(model), listOf(), listOf(), listOf()))
  return itemStack
}
