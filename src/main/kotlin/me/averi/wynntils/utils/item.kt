package me.averi.wynntils.utils

import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import net.minecraft.world.level.ItemLike

fun itemStackWithModel(model: Float, base: ItemLike = Items.POTION) = ItemStack(base).apply { customModel = model }
