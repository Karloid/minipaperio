class Move internal constructor() {

    var direction: Direction = Direction.DOWN

    var debug: String = ""


    internal fun send() {
        val outString = """{"command":"${direction.toString().toLowerCase()}","debug":"$debug"}"""
        MainKt.myDebugLog("cmd -> $direction")
        println(outString)
    }

    fun set(cmd: Direction) {

        direction = cmd
    }

    fun appendToDebug(debugMessage: String) {
        debug += debugMessage
        if (isLocal) {
            MainKt.myDebugLog(debugMessage)
        }
    }
}