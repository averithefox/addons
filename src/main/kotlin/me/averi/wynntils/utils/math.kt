package me.averi.wynntils.utils

import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.sqrt

fun easeOutCirc(t: Float): Float {
  val x = t.coerceIn(0f, 1f)
  return sqrt(1 - (x - 1f).pow(2f))
}

fun moveToward(current: Float, target: Float, maxStep: Float): Float = when {
  current < target -> min(current + maxStep, target)
  current > target -> max(current - maxStep, target)
  else -> current
}
