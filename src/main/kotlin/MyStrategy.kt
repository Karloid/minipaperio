//TODO plhillmap bus ifs for jump in the middle

//TODO keep best angle for buggy too
//test

class MyStrategy : Strategy {

    private var debugMessage: String = ""

    private lateinit var w: World
    private lateinit var move: Move


    var tick = 0

    var lastMove: Direction = Direction.UP

    private lateinit var m: MatchConfig

    private var s = State()

    override fun onMatchStarted(matchConfig: MatchConfig) {
        tick = 0
        this.m = matchConfig

        debugMessage += "\n"

        s = State()
    }

    override fun onNextTick(world: World, move: Move) {
        pretick(move, world)

        simpleStrategy()
    }

    private fun simpleStrategy() {


        lastMove = lastMove.toLeft()
        move.set(lastMove)

    }

    fun pretick(move: Move, world: World) {
        this.move = move;
        if (tick != 0) {
            world.processPre(this.w)
        } else {
            world.createMap()
        }
        this.w = world;
        w.calcMap()
        tick++

        if (!debugMessage.isEmpty()) {
            move.d(debugMessage)
            debugMessage = ""
        }
    }


    override fun onParsingError(message: String) {
        debugMessage = message
    }

}

private fun <T> Array<T>.random(): T {
    return get((this.size * Math.random()).toInt())
}

class State {
}
