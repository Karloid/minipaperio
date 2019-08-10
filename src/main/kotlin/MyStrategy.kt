//TODO delete archive before zip
//test
//TODO change to INTS
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

        //circle()
        val nextPoint = w.me.pos.applyDir(move.direction)
        logg("dir -> ${move.direction} to $nextPoint w.cells-> ${w.cells.get(nextPoint)}")

        painter.onEndTick()
    }

    private fun stayAway() {
        val myPos = w.me.pos

        val meOnMyTerr = onMyTerr(myPos)

        val adjacent = w.getAdjacent(myPos).filter { !w.me.direction.isOpposite(myPos.dirTo(it.pos)) }
                .filter { cell ->
                    //avoid enemies from facing on not my terr
                    if ((!meOnMyTerr || !onMyTerr(cell.pos)) && w.enPlayers.any { it.pos == cell.pos }) {
                        logg("avoid enemies from facing on not my terr, drop turn ${cell.pos}")
                        return@filter false
                    }

                    true
                }


        adjacent.filter { it.lines != null && it.lines != w.me }.forEach {
            val adjacent2 = w.getAdjacent(it.pos)
            val enemiesNear = w.enPlayers.filter { en -> adjacent2.any { it.pos == en.pos } }

            //no enemies with one step from theirs territory
            val noEndingEnemies = !enemiesNear.any { en ->
                w.getAdjacent(en.pos).any { it.territory == en }
            }
            if (noEndingEnemies) {
                logg("move at enemy line!")
                return moveTo(it)
            } else {
                logg("skip cutting line due enemies with one step from theirs territory")
            }
        }

        val notMyCells = adjacent.filter {
            it.territory != w.me && it.lines != w.me
        }.toMutableList()

        val myCells = adjacent.filter { it.territory == w.me }

        val notMyCellsAccess = HashMap<Point2D, PlainArray<Int>>()
        notMyCells.forEach {
            val access = w.calcAccess(it.pos, w.me, w.getAllPlayers(), listOf(it.pos))
            notMyCellsAccess[it.pos] = access
        }

        notMyCells.removeAll {
            val access = notMyCellsAccess[it.pos]!!

            val result = !(w.me.territory.asSequence().any { myTerr -> access.getFast(myTerr) < Int.MAX_VALUE })
            if (result) {
                logg("removed dead end cell $it")
            }
            result
        }

        //seek to base
        if (w.me.lines.size > 12 || getDistToEn() < 4 || (getMinDistFromEnToLine()) - 5 < getMinDistFromMeToMyTerr()) {
            moveToFarFromEnemy(myCells, notMyCells, notMyCellsAccess)
            return
        }

        if (notMyCells.isNotEmpty()) {
            notMyCells.sortedBy { canCell ->
                var sort = w.enPlayers.asSequence()
                        .flatMap {
                            it.territory.asSequence()
                        }.map {
                            w.getAccess(w.me).getFast(it)
                        }.min()?.toDouble()
                        ?: -1.0
                sort += w.getAdjacent(canCell.pos)
                        .count { it.lines == w.me || it.territory == w.me } * 2

                sort
            }.first().let {
                logg("move towards enemies territory")
                return moveTo(it)
            }
        }
        move.d("no more steps hmm")
        moveToFarFromEnemy(myCells, notMyCells, notMyCellsAccess)
    }

    private fun onMyTerr(myPos: Point2D) = w.cells.getFast(myPos).territory == w.me

    private fun getDistToEn(): Double {
        return w.enPlayers.asSequence().map { w.getAccess(it).getFast(w.me.pos) }.min()?.toDouble() ?: 100.0
    }

    private fun getMinDistFromMeToMyTerr(): Double {
        val access = w.getAccess(w.me)
        return w.me.territory.asSequence().map { myTerr -> access.getFast(myTerr) }.min()?.toDouble()!!
    }

    private fun getMinDistFromEnToLine() = w.enPlayers
            .asSequence()
            .map { enemy ->
                val access = w.getAccess(enemy)
                w.me.lines.asSequence()
                        .map { access.getFast(it) }.min()?.toDouble() ?: 1000.0
            }.min() ?: 100.0

    private fun moveToFarFromEnemy(myCells: List<MapCell>, notMyCells: List<MapCell>, notMyCellsAccess: HashMap<Point2D, PlainArray<Int>>) {
        val freeCellsAtBorder = w.me.territory.asSequence().flatMap { w.getAdjacent(it).asSequence().filter { it.territory != w.me } }.toList()

        val closestToTerr = freeCellsAtBorder.minBy { free ->
            var sort = w.enPlayers.asSequence().flatMap { it.territory.asSequence() }
                    .map { it.eucDist(free.pos) }.min() ?: 100.0

           // sort -= (w.enPlayers.asSequence().map { it.pos.eucDist(free.pos) }.min() ?: 1.0) / 2
            sort
        }
        val target = closestToTerr

        logg("moveToFarFromEnemy target is $target")

        myCells.sortedBy { canCell ->
            var value = target?.pos?.eucDist(canCell.pos) ?: 100.0

            value
        }.firstOrNull()?.let {
            move.d("moveToFarFromEnemy move to myCell far from enemy")
            moveTo(it)
            return
        }

        val getDistToMe: (MapCell) -> Int = { canCell ->
            val access = notMyCellsAccess.get(canCell.pos)!!

            w.me.territory.asSequence().map { access.getFast(it) }.min() ?: Int.MAX_VALUE
        }
        val variants = notMyCells.sortedBy(getDistToMe)
        variants
                .first()
                .let {
                    move.d("moveToFarFromEnemy move to my cells trough not my, variants")
                    if (isLocal) {
                        logg("variants " + variants.map { it.toString() + " SCORE " + getDistToMe(it) }.toString())
                    }
                    moveTo(it)
                }
    }

    private fun moveToBase(myCells: List<MapCell>, notMyCells: List<MapCell>, notMyCellsAccess: HashMap<Point2D, PlainArray<Int>>) {
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
            move.d("moveToBase move to myCell closest to enemy")
            moveTo(it)
            return
        }

        notMyCells.sortedBy { canCell ->
            val access = notMyCellsAccess.get(canCell.pos)!!

            w.me.territory.asSequence().map { access.getFast(it) }.min() ?: Int.MAX_VALUE
        }
                .first()
                .let {
                    move.d("moveToBase move to my cells trough not my")
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
