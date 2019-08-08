//TODO delete archive before zip
//test

class MyStrategy : Strategy {

    private var debugMessage: String = ""

    lateinit var w: World
    lateinit var move: Move


    var tick = 0

    var lastMove: Direction = Direction.UP

    private lateinit var m: MatchConfig

    var painter: MyStrategyPainter = EmptyPaintner()

    private var s = State()

    override fun onMatchStarted(matchConfig: MatchConfig) {
        tick = 0
        this.m = matchConfig

        debugMessage += ","

        s = State()

        painter.onInitializeStrategy()
    }

    override fun onNextTick(world: World, move: Move) {
        pretick(move, world)

        painter.onStartTick()

        // simple()
        stayAway()

        painter.onEndTick()
        //circle()
    }

    private fun stayAway() {
        val myPos = w.me.pos

        val adjacent = w.getAdjacent(myPos).filter { !w.me.direction.isOpposite(myPos.dirTo(it.pos)) }


        adjacent.firstOrNull { it.lines != null && it.lines != w.me }?.let {
            logg("move at enemy line!")
            return moveTo(it)
        }

        val notMyCells = adjacent.filter { it.territory != w.me && it.lines != w.me }
        val myCells = adjacent.filter { it.territory == w.me }

        //seek to base
        if (w.me.lines.size > 12 || getDistToEn() < 4 ||(getMinDistFromEnToLine()) - 3 < getMinDistFromMeToMyTerr()) {
            moveToBase(myCells, notMyCells)
            return
        }

        if (!notMyCells.isEmpty()) {
            //if (Math.random() > 0.7) {
            //    notMyCells.random().let {
            //        logg("move to random")
            //        return moveTo(it)
            //    }
            //}
            notMyCells.sortedBy { canCell ->
                var sort = w.enPlayers.asSequence().flatMap { it.territory.asSequence() }.map { it.eucDist(canCell.pos) }.min()
                        ?: -1.0
                sort += w.getAdjacent(canCell.pos)
                        .count { it.lines == w.me || it.territory == w.me } * 2

                sort
            }.first().let {
                logg("move to enemies territory")
                return moveTo(it)
            }
            return
        }
        move.d("no more steps hmm")
        moveToFarFromEnemy(myCells, notMyCells)

    }

    private fun getDistToEn(): Double {
        return w.enPlayers.asSequence().map { it.pos.eucDist(w.me.pos) }.min() ?: 100.0
    }

    private fun getMinDistFromMeToMyTerr(): Double = w.me.territory.asSequence().map { it.eucDist(w.me.pos) }.min()!!
    private fun getMinDistFromEnToLine() = w.enPlayers
            .asSequence()
            .map { enemy ->
                w.me.lines.asSequence()
                        .map { it.eucDist(enemy.pos) }.min() ?: 1000.0
            }.min() ?: 100.0

    private fun simple() {
        val myPos = w.me.pos

        val adjacent = w.getAdjacent(myPos).filter { !w.me.direction.isOpposite(myPos.dirTo(it.pos)) }


        adjacent.firstOrNull { it.lines != null && it.lines != w.me }?.let {
            logg("move at enemy line!")
            return moveTo(it)
        }

        val notMyCells = adjacent.filter { it.territory != w.me && it.lines != w.me }
        val myCells = adjacent.filter { it.territory == w.me }

        //seek to base
        if (w.me.lines.size > 5) {
            moveToBase(myCells, notMyCells)
            return
        }

        if (!notMyCells.isEmpty()) {
            moveOutEnemyLines(notMyCells)
            return
        }
        move.d("no more steps hmm")
        moveToBase(myCells, notMyCells)
    }

    private fun moveToFarFromEnemy(myCells: List<MapCell>, notMyCells: List<MapCell>) {
        val freeCellsAtBorder = w.me.territory.asSequence().flatMap { w.getAdjacent(it).asSequence().filter { it.territory != w.me } }.toList()
        val farestCell = freeCellsAtBorder.maxBy { free ->
            w.enPlayers.asSequence().map { it.pos.eucDist(free.pos) }.min() ?: 100.0
        }
        val closestToTerr = freeCellsAtBorder.minBy { free ->
            var sort = w.enPlayers.asSequence().flatMap { it.territory.asSequence() }
                    .map { it.eucDist(free.pos) }.min() ?: 100.0

            sort -= (w.enPlayers.asSequence().map { it.pos.eucDist(free.pos) }.min() ?: 1.0) * 2
            sort
        }
        val target = closestToTerr

        myCells.sortedBy { canCell ->
            var value = target?.pos?.eucDist(canCell.pos) ?: 100.0

            value
        }.firstOrNull()?.let {
            move.d("move to myCell far from enemy")
            moveTo(it)
            return
        }

        notMyCells.sortedBy { canCell ->
            w.me.territory.asSequence().map { it.eucDist(canCell.pos) }.min() ?: -1.0
        }
                .first()
                .let {
                    move.d("move to my cells trough not my")
                    moveTo(it)
                }

    }

    private fun moveToBase(myCells: List<MapCell>, notMyCells: List<MapCell>) {
        myCells.sortedBy { canCell ->
            var value = w.enPlayers.asSequence()
                    .flatMap { it.territory.asSequence() + it.lines }
                    .map { it.eucDist(canCell.pos) }
                    .min() ?: -1.0
            if (value < 0) {
                value = w.cells.array.map {
                    val sort = it.territory.isNull().then { it.pos.eucDist(canCell.pos) } ?: Double.MAX_VALUE
                    return@map sort
                }.min()!!
            }

            value
        }.firstOrNull()?.let {
            move.d("move to myCell closest to enemy")
            moveTo(it)
            return
        }

        notMyCells.sortedBy { canCell ->
            w.me.territory.asSequence().map { it.eucDist(canCell.pos) }.min() ?: -1.0
        }
                .first()
                .let {
                    move.d("move to my cells trough not my")
                    moveTo(it)
                }
    }

    private fun moveToEnemyLines(notMyCells: List<MapCell>) {
        notMyCells.sortedBy { canCell ->
            w.enPlayers.asSequence().flatMap { it.lines.asSequence() }
                    .map { it.eucDist(canCell.pos) }.min()
                    ?: -1.0
        }.first().let {
            logg("move to enemy lines")
            return moveTo(it)
        }
    }

    private fun moveOutEnemyLines(notMyCells: List<MapCell>) {
        notMyCells.sortedBy { canCell ->
            w.enPlayers.asSequence().flatMap { it.lines.asSequence() }
                    .map { it.eucDist(canCell.pos) }.min()
                    ?: -1.0
        }.last().let {
            logg("move out from enemy lines")
            return moveTo(it)
        }
    }

    private fun moveOutFromEnemies(notMyCells: List<MapCell>) {

    }


    private fun logg(s: String) {
        move.d(s)
    }

    private fun moveTo(it: MapCell) {
        move.set(w.me.pos.dirTo(it.pos))
    }

    private fun circle() {
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


    private fun Move.d(s: String) {
        appendToDebug("${w.tick}> $s")
    }
}


fun Any?.isNull(): Boolean {
    return this == null
}

private fun Point2D.getAdjacent() {

}

fun <T> Array<T>.random(): T {
    return get((this.size * Math.random()).toInt())
}

class State {
}
