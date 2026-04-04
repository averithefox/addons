package me.averi.wynntils

enum class Spell(val clicks: BooleanArray) {
  FIRST(booleanArrayOf(true, false, true)),
  SECOND(booleanArrayOf(true, true, true)),
  THIRD(booleanArrayOf(true, false, false)),
  FOURTH(booleanArrayOf(true, true, false))
}