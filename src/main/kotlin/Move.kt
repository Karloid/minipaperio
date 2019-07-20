import org.json.JSONObject


class Move internal constructor() {

    var command: String = "stop"

    var debug: String = ""


    internal fun send() {
        val json = JSONObject(this)
        JsonIO.writeToStdOut(json)
    }

    fun set(cmd: Int) {

        command = when (cmd) {
            -1 -> {
                "left"
            }
            0 -> {
                "stop"
            }
            else -> {
                "right"
            }
        }
        d(command)
    }

    fun d(debugMessage: String) {
        debug += "\n" + debugMessage
        if (isLocal) {
            System.err.println(debugMessage)
        }
    }
}