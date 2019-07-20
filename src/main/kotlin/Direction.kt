enum class Direction {
    LEFT,
    UP,
    RIGHT,
    DOWN;

    fun toRight(): Direction {
        return next(1)
    }

    fun toLeft(): Direction {
        return next(-1)
    }


    private fun next(diff: Int): Direction {
        var indexOf = cValues.indexOf(this)
        indexOf += diff
        if (indexOf >= cValues.size) {
            indexOf = 0
        } else if (indexOf < 0) {
            indexOf = cValues.size - 1
        }
        return cValues.get(indexOf)
    }

    companion object {
        fun fromString(s: String): Direction {
            return when (s) {
                "left" -> LEFT
                "up" -> UP
                "right" -> RIGHT
                "down" -> DOWN
                else -> {
                    MainKt.myDebugLog("invalid string for direction $s")
                    LEFT
                }
            }
        }

        val cValues = values()
    }
}