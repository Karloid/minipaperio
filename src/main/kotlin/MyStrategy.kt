import java.lang.Math.abs

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

    public var targetToDrawDebug: MapCell? = null

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
        logg("dir -> ${move.direction} to $nextPoint w.cells-> ${w.cells.get(nextPoint)?.pos}")
        lastMove = move.direction
        painter.onEndTick()
    }

    private fun stayAway() {
        val myPos = w.me.pos

        val meOnMyTerr = onMyTerr(myPos)
        val allEnemiesDead = w.enPlayers.isEmpty()

        val adjacent = w.getAdjacent(myPos).filter { !w.me.direction.isOpposite(myPos.dirTo(it.pos)) }
                .filter { cell ->
                    if (cell.lines == w.me) {
                        return@filter false
                    }

                    //avoid enemies from facing on not my terr
                    if (((!meOnMyTerr && isOnEnemy(cell.pos)) || isOnEnemy(cell.pos)) && w.enPlayers.any { en ->
                                var result = en.pos.eucDist(cell.pos) < 2.1     // < TODO check, mb change ack to 1.1
                                if (result && en.pos.eucDist(cell.pos) > 0.1) {
                                    if (cell.pos.dirTo(en.pos) == (en.direction)) {
                                        logg("its cell is opossite to en direction, is safe ${cell.pos}")
                                        result = false
                                    }
                                }
                                result
                            }) {
                        logg("avoid enemies from facing on not my terr, drop turn ${cell.pos} ${w.me.pos.dirTo(cell.pos)}")
                        return@filter false
                    }

                    true
                }


        adjacent.filter { it.lines != null && it.lines != w.me }.forEach { adj ->
            val adjacent2 = w.getAdjacent(adj.pos)
            val enemiesNear = w.enPlayers.filter { en -> adjacent2.any { it.pos == en.pos } }

            //no enemies with one step from theirs territory
            var noEndingEnemies = enemiesNear.none() { en ->
                w.getAdjacent(en.pos).any { it.territory == en }
            }
            val dangerEnemy = w.enPlayers.firstOrNull() { enemy -> w.getAccess(enemy).getFast(adj.pos) <= 1.1 && enemy.lines.size <= w.me.lines.size }
            if (dangerEnemy != null) {
                logg("there is enemy which can kill us by lines length $dangerEnemy, not going to enemy line $adj")
                noEndingEnemies = false
            }
            if (noEndingEnemies) {
                logg("move at enemy line!")
                return moveTo(adj)
            } else {
                logg("skip cutting line due enemies with one step from theirs territory")
            }
        }

        val notMyCells = adjacent.filter {
            it.territory != w.me && it.lines != w.me
        }.toMutableList()

        val myCells = adjacent.filter { it.territory == w.me }.toMutableList()

        val notMyCellsAccess = HashMap<Point2D, PlainArray<Int>>()
        notMyCells.forEach {
            val access = w.calcAccess(it.pos, w.me, w.getAllPlayers(), listOf(w.me.pos))
            notMyCellsAccess[it.pos] = access
        }

        val backupNotMyCells = ArrayList(notMyCells)

        val removedDueSoftDeadEnd = mutableListOf<MapCell>()
        notMyCells.removeAll {
            val access = notMyCellsAccess[it.pos]!!

            var result = !(w.me.territory.asSequence().any { myTerr -> access.getFast(myTerr) < Int.MAX_VALUE })
            if (result) {
                logg("removed dead end cell $it")
            }

            if (!result && !meOnMyTerr && getDistToEn() > 6) {
                val accessWOterr = w.calcAccess(it.pos, w.me, w.getAllPlayers(), w.me.territory + w.me.pos)
                var accessablePos = calcAccessCount(accessWOterr)
                if (accessablePos < x_cells_count * y_cells_count / 2) {
                    logg("removed due soft(myTerr) dead end cell $it")
                    result = true
                    removedDueSoftDeadEnd.add(it)
                }
            }
            result
        }
        if (notMyCells.isEmpty() && backupNotMyCells.isNotEmpty()) {
            logg("restore notMyCells with $backupNotMyCells due lack of steps $backupNotMyCells")
            notMyCells.addAll(backupNotMyCells)
        }

        val currentAccessScope = w.calcAccess(w.me.pos, w.me, w.getAllPlayers(), w.me.territory)

        var accessablePos = calcAccessCount(currentAccessScope)

        if (!meOnMyTerr && accessablePos < x_cells_count * y_cells_count / 2) {
            logg("moveToBaseOrInsideBase due we in loop")
            moveBackToBase(myCells, notMyCells, notMyCellsAccess, removedDueSoftDeadEnd)
            return
        }

        //seek to base
        if (isLineToLong(allEnemiesDead) || enemyIsTooClose() || enemyToCloseToMyLine() || (!meOnMyTerr && pathToBaseIsDangerous())) {
            if (meOnMyTerr) {
                moveToBaseOrInsideBase(myCells, notMyCells, notMyCellsAccess)
            } else {
                moveBackToBase(myCells, notMyCells, notMyCellsAccess, removedDueSoftDeadEnd)
            }
            return
        }

        if (notMyCells.isNotEmpty()) {
            var sortFun: (MapCell) -> Double = { canCell ->
                val access = notMyCellsAccess[canCell.pos]!!

                val minDistToMyTerr = w.me.territory.asSequence().map { access.getFast(it) }.min() ?: 10

                val keepDistFroMyTerr = allEnemiesDead.then { 6 } ?: 4

                var enKoeff = getMinDistFromEn(canCell.pos).toDouble()
                if (enKoeff > 5) {
                    enKoeff = 0.0
                } else {
                    enKoeff /= 100.0
                }

                if (enKoeff > 10) {
                    enKoeff *= -1
                }

                var result = abs(keepDistFroMyTerr - minDistToMyTerr).toDouble()

                //result -= (canCell.territory != null).then { 0.3 } ?: 0.0

                result = result + (enKoeff)

                val dir = w.me.pos.dirTo(canCell.pos)
                if (dir == lastMove) {
                    result -= 0.1
                }

                logg("result $result, enemy koeff $enKoeff for $canCell $dir")

                result
            }
            val bonusToPick: Bonus? = getBonusToPick()
            if (bonusToPick != null) {
                val accessBonus = w.calcAccess(bonusToPick.pos, w.me, w.getAllPlayers(), null)
                sortFun = { canCell ->
                    accessBonus.getFast(canCell.pos).toDouble()
                }
                logg("Going to bonus ! $bonusToPick")
            }

            val notMySortedByDistToMe = notMyCells.sortedBy(sortFun)

            notMySortedByDistToMe.first().let {
                logg("move to catch territory with keep distance ${sortFun(it)}")
                return moveTo(it)
            }
        }
        move.d("no more steps hmm")
        moveToBaseOrInsideBase(myCells, notMyCells, notMyCellsAccess)
    }

    private fun isOnEnemy(pos: Point2D): Boolean {
        return w.cells.getFast(pos).territory?.let { it != w.me } == true
    }

    private fun pathToBaseIsDangerous(): Boolean {
        if (w.enPlayers.isEmpty()) {
            return false
        }

        val access = w.getAccess(w.me)
        val closestMyTerr = w.me.territory.minBy { access.getFast(it) } ?: return false

        if (access.getFast(closestMyTerr) == Int.MAX_VALUE) {
            logg("!! no path to my terr")
            return false
        }
        val path = mutableListOf(closestMyTerr)
        var curPos = closestMyTerr

        while (true) {
            curPos = w.getAdjacent(curPos).minBy { access.getFast(it.pos) }?.pos ?: break
            path.add(curPos)
            if (access.getFast(curPos) == 0) {
                break
            }
        }
        path.forEach { pathNode ->
            val myTicks = access.getFast(pathNode)
            val enTicks = w.enPlayers.map { w.getAccess(it).getFast(pathNode) }.min() ?: 1000

            if (myTicks + 4 > enTicks) {
                logg("found dangerous point $pathNode, myTicks $myTicks, enTicks $enTicks, fullPath $path")
                return true
            }
        }

        return false
    }

    private fun enemyToCloseToMyLine(): Boolean {
        val minDistFromEnToLine = getMinDistFromEnToLine()
        val minDistFromMeToMyTerr = getMinDistFromMeToMyTerr()
        val result = minDistFromEnToLine - 3 < minDistFromMeToMyTerr
        result.then { logg("! enemyToCloseToMyLine minDistFromEnToLine $minDistFromEnToLine, minDistFromMeToMyTerr $minDistFromMeToMyTerr") }
        return result
    }

    private fun enemyIsTooClose(): Boolean {
        val dist = getDistToEn()
        val result = dist < 4
        result.then { logg("enemyIsTooClose $dist") }
        return result
    }

    private fun isLineToLong(allEnemiesDead: Boolean): Boolean {
        val result = w.me.lines.size > allEnemiesDead.then { 14 } ?: 8
        result.then { logg("isLineToLong ${w.me.lines.size}") }
        return result
    }

    private fun calcAccessCount(currentAccessScope: PlainArray<Int>): Int {
        var accessablePos = 0
        currentAccessScope.fori { x, y, v ->
            if (v < Int.MAX_VALUE) {
                accessablePos++;
            }
        }
        return accessablePos
    }

    private fun getMinDistFromEn(pos: Point2D): Int {
        return w.enPlayers.asSequence().map { w.getAccess(it).getFast(pos) }.min() ?: 31
    }

    private fun moveBackToBase(myCells: MutableList<MapCell>, notMyCells: MutableList<MapCell>, notMyCellsAccess: HashMap<Point2D, PlainArray<Int>>, removedDueSoftDeadEnd: MutableList<MapCell>) {
        removedDueSoftDeadEnd.forEach {
            if (it.territory == w.me) {
                logg("moveBackToBase restore dead soft end to myCells $it")
                myCells.add(it)
            } else {
                logg("moveBackToBase restore dead soft end to notMyCell $it")
                notMyCells.add(it)
            }
        }

        if (!myCells.isEmpty()) {
            logg("moveBackToBase move just into my cells")
            moveTo(myCells.first())
            return
        }

        notMyCells.sortedBy { notMyCell ->
            val access = notMyCellsAccess[notMyCell.pos]!!


            var distToMe = w.me.territory.asSequence().map { terr -> access.getFast(terr) }.min() ?: 1000

            val distFromEnToThisCell = (w.enPlayers.map { en -> w.getAccess(en).getFast(notMyCell.pos) }.min()?.toDouble()
                    ?: 100.0) / 100.0


            var result = distToMe - distFromEnToThisCell

            val dir = w.me.pos.dirTo(notMyCell.pos)
            if (dir == lastMove) {
                result -= 0.1
            }
            return@sortedBy result
        }.firstOrNull()?.let {
            logg("moveBackToBase move to closest to my terr cell")
            moveTo(it)
            return
        }
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
                w.me.lines.asSequence().plus(w.me.pos)
                        .map { access.getFast(it) }.min()?.toDouble() ?: 1000.0
            }.min() ?: 100.0

    private fun moveToBaseOrInsideBase(myCells: List<MapCell>, notMyCells: List<MapCell>, notMyCellsAccess: HashMap<Point2D, PlainArray<Int>>) {
        val freeCellsAtBorder: List<MapCell> = w.me.territory.asSequence().flatMap { w.getAdjacent(it).asSequence().filter { it.territory != w.me } }.toList()

        var candidateTargets = freeCellsAtBorder.filter { cell ->
            (w.enPlayers.asSequence().map {
                getDistFromPlayer(it, cell)
            }.min() ?: 100) > w.getAccess(w.me).getFast(cell.pos) + 4
        }.filter { candidate -> w.enPlayers.none { it.pos == candidate.pos } }

        if (candidateTargets.isEmpty()) {
            logg("all my territory is to close to enemy, go rush!")
            candidateTargets = freeCellsAtBorder
        }

        var target: MapCell?

        target = candidateTargets.minBy { free ->

            if (w.enPlayers.isNotEmpty()) {
                var enTerrDist = w.enPlayers.asSequence().flatMap { it.territory.asSequence() }
                        .map { it.eucDist(free.pos) }.min() ?: 100.0
                enTerrDist -= w.getAdjacent(free.pos).count { isOnEnemy(it.pos) } * 1.5f
                enTerrDist -= isOnEnemy(free.pos).then { 1.5 } ?: 0.0

                enTerrDist += w.getAccess(w.me).getFast(free.pos) / 100.0
                enTerrDist -= (w.enPlayers.map { getDistFromPlayer(it, free) }.min()?.toDouble() ?: 100.0) / 100.0

                if (w.getMyAccess().getFast(free.pos)==1 && w.enPlayers.any{w.getAccess(it).getFast(free.pos) == 2}) {
                    enTerrDist += 30 //banned
                    logg("dangereous move, banned $free ${getDirTo(free)}")
                }


                enTerrDist
            } else {
                w.getAccess(w.me).getFast(free.pos).toDouble()
            }
        }

        this.targetToDrawDebug = target
        val bonusToPick: Bonus? = getBonusToPick()
        if (bonusToPick != null) {
            target = w.cells.get(bonusToPick.pos)
            logg("Going to bonus inside base ! $bonusToPick")
        }

        logg("moveToBaseOrInsideBase target is $target")

        (myCells + notMyCells).sortedBy { canCell ->
            var value = target?.pos?.eucDist(canCell.pos) ?: 100.0
            value -= getMinDistFromEn(canCell.pos) / 100.0
            if (w.getMyAccess().getFast(canCell.pos)==1 && w.enPlayers.any{w.getAccess(it).getFast(canCell.pos) == 2}) {
                value += 30 //banned
                logg("dangereous move to target, banned $canCell ${getDirTo(canCell)}")
            }
            value
        }.firstOrNull()?.let {
            move.d("moveToBaseOrInsideBase move to target $target")
            moveTo(it)
            return
        }

        val getDistToMe: (MapCell) -> Int = { canCell ->
            val access = notMyCellsAccess.get(canCell.pos)!!

            w.me.territory.asSequence().map { access.getFast(it) }.min() ?: Int.MAX_VALUE
        }
        val variants = notMyCells.sortedBy(getDistToMe)
        variants
                .firstOrNull()
                ?.let {
                    move.d("moveToBaseOrInsideBase move to my cells trough not my, variants")
                    if (isLocal) {
                        logg("variants " + variants.map { it.toString() + " SCORE " + getDistToMe(it) }.toString())
                    }
                    moveTo(it)
                } ?: logg("moveToBaseOrInsideBase empty collections! !")
    }

    private fun getDirTo(free: MapCell): Direction {
        return w.me.pos.dirTo(free.pos)
    }

    private fun getDistFromPlayer(it: Player, cell: MapCell) = w.getAccess(it).getFast(cell.pos)

    private fun getBonusToPick() =
            w.bonuses.filter { it.type != BonusType.SLOW && w.getMyAccess().getFast(it.pos) <= 18 }.minBy { w.getMyAccess().getFast(it.pos) }

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
        targetToDrawDebug = null
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


/**
 * Найти множество точек охватываемых для заливки очень просто.

Берем матрицу размерами W*H равными размерами поля.

В каждую ячейку пишем значение 0 (что значит NOT_FILLED)

Для всех ячеек территории игрока и его шлейфа пишем значение 1 (т.е. INNER)

Берем очередь queue, в которую заносим все клетки, которые находятся у границы карты (т.е. x==0 || y == 0 || x == W - 1 || y == H - 1) у которых значение NOT_FILLED

Заливаем от границ всю карту - для каждого значения из queue берем соседей, если там значение NOT_FILLED, то добавляем его в queue, и закрашиваем в OUTER = 2:

while (!queue.empty())
{
CellId p = queue.pop_front();

for (int dir = 0; dir < 4; ++dir)
{
XY newP = getNeigbour(p, (Direction) dir);
if (isValidXY(newP))
{
if (cells[newP] == NOT_FILLED)
{
cells[newP] = OUTER;
queue.push_back(newP);
}
}
}
}
6.Итого все клетки, у которых останется значение NOT_FILLED, те нас и интересуют (плюс к ним значения из шлейфа)
 */