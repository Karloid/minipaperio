import org.json.JSONObject

var isLocal: Boolean = false

object MainKt {
    private var robot: Strategy = MyStrategy()


    private var npeCount: Int = 0

    @JvmStatic
    fun main(args: Array<String>) {
        isLocal = args.size > 0

        var gameMessage: JSONObject?
        gameMessage = JsonIO.readFromStdIn() ?: throw NullPointerException("game message is null!")
        val config = MatchConfig(gameMessage)
        robot.onMatchStarted(config)
        while (true) {
            try {
                gameMessage = JsonIO.readFromStdIn() ?: throw NullPointerException("game message is null!")

                myDebugLog(gameMessage.toString())

                val tickState = World(gameMessage.getJSONObject("params"))
                val move = Move()
                robot.onNextTick(tickState, move)
                move.send()
            } catch (e: Exception) {
                if (isLocal) {
                    e.printStackTrace()
                }
                robot.onParsingError(e.message ?: "unknown")
                val move = Move()
                move.set(Direction.UP)
                move.send()

                Thread.sleep(10)
                if (e is NullPointerException) {
                    npeCount++
                }
                if (npeCount > 100 || isLocal) {
                    //probably it is the end
                    println("GG WP bye bye")
                    return
                }
            }
        }
    }

    fun myDebugLog(outMsg: String) {
        if (isLocal) {
            System.err.println("myDebug$outMsg")
        }
    }

}

private fun <E> List<E>.random(): E {
    return get((this.size * Math.random()).toInt())
}
