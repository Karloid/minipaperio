class Move internal constructor() {

    var direction: Direction = Direction.DOWN

    var debug: String = ""


    internal fun send() {
        val outString = """{"command":"${direction.toString().toLowerCase()}","debug":"$debug"}"""
        MainKt.myDebugLog("my answer $outString")
        println(outString)
    }

    fun set(cmd: Direction) {

        direction = cmd

        d(direction.toString())
    }

    fun d(debugMessage: String) {
        debug += debugMessage
        if (isLocal) {
            System.err.println(debugMessage)
        }
    }
}

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
        indexOf = indexOf + diff
        if (indexOf >= cValues.size) {
            indexOf = 0
        } else if (indexOf < 0) {
            indexOf = cValues.size
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