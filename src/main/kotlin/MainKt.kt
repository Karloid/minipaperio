import org.json.JSONObject
import java.util.*

var isLocal: Boolean = false
object MainKt {
    private var robot: Strategy = MyStrategy()


    private var npeCount: Int = 0

    @JvmStatic
    fun main(args: Array<String>) {
        isLocal = args.size > 0

        val commands = listOf("left", "right", "up", "down")
        val scanner = Scanner(System.`in`)

        while (true) {
            val input = scanner.next()
            System.out.print("{\"command\": \"${commands.random()}\"}\n")
        }
        
        var gameMessage: JSONObject?

        while (true) {
            try {
                gameMessage = JsonIO.readFromStdIn() ?: throw NullPointerException("game message is null!")

                val messageType = gameMessage.getEnum(MessageType::class.java, "type") ?: throw NullPointerException("messageType is null!")

                when (messageType) {
                    MessageType.tick -> {
                        val tickState = World(gameMessage.getJSONObject("params"))
                        val move = Move()
                        robot.onNextTick(tickState, move)
                        move.send()
                    }

                    MessageType.new_match -> {
                        if (isLocal) {
                            System.err.println(gameMessage.toString(2))
                        }
                        val matchConfig = MatchConfig(gameMessage.getJSONObject("params"))
                        robot.onMatchStarted(matchConfig)
                    }
                }
            } catch (e: Exception) {
                if (isLocal) {
                    e.printStackTrace()
                }
                robot.onParsingError(e.message ?: "unknown")
                val move = Move()
                move.set(0)
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

}

private fun <E> List<E>.random(): E {
   return get((this.size * Math.random()).toInt())
}
