import org.json.JSONObject

class Move internal constructor() {

    var direction: Direction = Direction.DOWN

    var debug: String = ""


    internal fun send() {
        val outString = JSONObject()
                .put("command", direction.toString().toLowerCase())
                .put("debug", debug)
                .toString()

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