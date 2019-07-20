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