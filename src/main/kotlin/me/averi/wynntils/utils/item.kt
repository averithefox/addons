package me.averi.wynntils.utils

import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items

fun itemStackWithModel(model: Float) = ItemStack(Items.POTION).apply { customModel = model }
