
import org.json.JSONObject


var isLocal: Boolean = false

object MainKt {
    private var strategy: MyStrategy = MyStrategy()


    private var npeCount: Int = 0

    @JvmStatic
    fun main(args: Array<String>) {
        isLocal = args.contains("local_debug")

        var gameMessage: JSONObject?
        gameMessage = JsonIO.readFromStdIn() ?: throw NullPointerException("game message is null!")
        val config = MatchConfig(gameMessage)
        isLocal.then {
            val rewindClientWrapper = RewindClientWrapper()
            strategy.painter = rewindClientWrapper
            rewindClientWrapper.setMYS(strategy)
        }
        strategy.onMatchStarted(config)
        while (true) {
            try {
                gameMessage = JsonIO.readFromStdIn() ?: throw NullPointerException("game message is null!")
               // (Math.random() > 0.5).then { throw RuntimeException() }
                val tickState = World(gameMessage.getJSONObject("params"), config)
                val move = Move()
                strategy.onNextTick(tickState, move)
                move.send()
            } catch (e: Exception) {
                if (isLocal) {
                    e.printStackTrace()
                }

                strategy.onParsingError(e.message ?: "unknown")
                val move = Move()
                move.set(strategy.lastMove)

                move.appendToDebug("got exception $e \n${getStracktrace(e)}")
                move.send()

                Thread.sleep(10)
                if (e is NullPointerException) {
                    npeCount++
                }
                if (isLocal) {
                    //probably it is the end
                    println("GG WP bye bye")
                    return
                }
            }
        }
    }

    fun myDebugLog(outMsg: String) {
        if (isLocal) {
            System.err.println(": $outMsg")
        }
    }
}

