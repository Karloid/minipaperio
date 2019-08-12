import java.awt.Color

class RewindClientWrapper : MyStrategyPainter {

    private lateinit var mys: MyStrategy
    private lateinit var rc: RewindClient
    private val didDrawPP: Boolean = false

    override fun setMYS(myStrategy: MyStrategy) {
        mys = myStrategy
    }

    override fun onStartTick() {
        val allPlayers = mys.w.enPlayers + mys.w.me
        allPlayers.fori {
            drawCells(it.territory, terrColor(it), terrPadding)
        }
        allPlayers.fori {
            drawCells(it.lines, lineColor(it), linePadding)
        }
        allPlayers.fori { it ->
            drawCell(it.pos, playerColor(it), 8)
            if (itsMe(it)) {
                val pos = it.pos
                val x1 = pos.x.toDouble() * cellSize + 4
                val y1 = pos.y.toDouble() * cellSize + 4
                rc.message("pos: $pos")
                //rc.message("debug cellSize:$cellSize x1 $x1, y1 $y1, ${x1 + cellSize - 4 * 2}, ${y1 + cellSize - 4 * 2}, Color, commodityLayer")
            }
            drawCellDir(it.pos, it.direction, playerColor(it))
        }


        //drawCell(Point2D(0,0), Color.GRAY, 0);
        //  drawCell(Point2D(10,5), Color.GREEN, 0);
        rc.message("my pos ${mys.w.me.pos}")

        allPlayers.sortedBy { it.score }.fori {
            rc.message("${it.score}${(itsMe(it)).then { "!" } ?: ""}")
        }


        mys.w.bonuses.forEach {
            drawCell(it.pos, getBonusColor(it), cellSize / 5)
        }
    }

    private fun getBonusColor(bonus: Bonus): Color {
        return when (bonus.type) {
            BonusType.SAW -> Color.MAGENTA
            BonusType.SLOW -> Color.yellow
            BonusType.NITRO -> Color.orange
        }
    }

    private fun itsMe(it: Player) = it == mys.w.me

    private val commodityLayer = 3

    private fun drawCellDir(it: Point2D, direction: Direction, color: Color) {
        val padding = cellSize / 3
        var x1 = it.x.toDouble() * cellSize + padding
        var y1 = it.y.toDouble() * cellSize + padding

        when (direction) {
            Direction.LEFT -> x1 -= padding
            Direction.UP -> y1 += padding
            Direction.RIGHT -> x1 += padding
            Direction.DOWN -> y1 -= padding
        }

        rc.rect(x1, y1, x1 + cellSize - padding * 2, y1 + cellSize - padding * 2, color, commodityLayer)
    }

    private fun playerColor(it: Player): Color {
        return (it == mys.w.me).then { myPlayerColor } ?: enPlayerColor
    }

    private fun lineColor(it: Player): Color {
        return (it == mys.w.me).then { myLineColor } ?: enLineColor
    }

    private fun drawCells(cells: List<Point2D>, color: Color, padding: Int) {
        cells.fori {
            drawCell(it, color, padding)
        }
    }

    private fun drawCell(it: Point2D, color: Color, padding: Int) {
        val x1 = it.x.toDouble() * cellSize + padding
        val y1 = it.y.toDouble() * cellSize + padding
        rc.rect(x1, y1, x1 + cellSize - padding * 2, y1 + cellSize - padding * 2, color, commodityLayer)
    }

    private fun terrColor(it: Player): Color {
        return enTerrColors[it.id]!!
    }

    override fun onEndTick() {

        val from = mys.w.me.pos

        //line(from, from.applyDir(mys.move.direction), Color.BLUE)

        mys.targetToDrawDebug?.let {
            drawCell(it.pos, Color.black, cellSize / 5)
        }

        rc.message("\nmove is ${mys.move.direction}")
        rc.message("msg-: ${mys.move.debug}")
        rc.endFrame()
    }

    private fun line(from: Point2D, to: Point2D, color: Color) {
        val x1 = (from.x * cellSize + cellSize / 2).toDouble()
        val y1 = (from.y * cellSize + cellSize / 2).toDouble()
        val x2 = (to.x * cellSize + cellSize / 2).toDouble()
        val y2 = (to.y * cellSize + cellSize / 2).toDouble()

        rc.line(
                x1, y1,
                x2, y2,
                color,
                commodityLayer
        )

        val r = cellSize / 8.toDouble()
        rc.circle(x1, y1, r, color, 2)
        rc.circle(x2, y2, r, color, 2)

        rc.message("line $from to $to")
    }

    override fun onInitializeStrategy() {
        try {
            rc = RewindClient()
        } catch (e: Exception) {
            e.printStackTrace()
            mys.painter = EmptyPaintner()
            return
        }

        cellSize = 1024 / x_cells_count
        terrPadding = ((terrPadding.toDouble() / 32) * cellSize).toInt()
        linePadding = ((linePadding.toDouble() / 32) * cellSize).toInt()

        rc.setMirrorSize(cellSize * y_cells_count)
    }

    override fun drawMove() {

    }

    companion object {
        var cellSize = 32
        var terrPadding = 2
        var linePadding = 8

        private val myLineColor = Color(0x84, 0x86, 0xC8)
        private val enLineColor = Color(0xC8, 0x96, 0x9C)

        private val myTerrColor = Color(0x52, 0x55, 0xC8)
        private val enTerrColor = Color(0xC8, 0x5E, 0x5B)

        private val enTerrColors = mapOf(
                "1" to Color(0x00, 0x00, 0xff),
                "2" to Color(0xC8, 0xaE, 0x5B),
                "3" to Color(0xC8, 0x5E, 0x5B),
                "4" to Color(0x18, 0x5E, 0x5B),
                "5" to Color.gray,
                "6" to Color.cyan,
                "i" to Color(0x52, 0x55, 0xC8)
        )

        private val myPlayerColor = Color.BLUE
        private val enPlayerColor = Color.RED

        val COLOR_CLEAR_AND_SELECT = Color(100, 100, 100, 100)
        val COLOR_ADD_TO_SELECT = Color(0, 100, 18, 128)
        val COLOR_MOVE = Color(6, 100, 0, 128)
        val COLOR_MY_GROUP = Color(3, 182, 0, 153)
        val COLOR_MOVE_POINT = Color(211, 2, 23, 255)
        val COLOR_FACILITY = Color(211, 147, 0, 255)
        val COLOR_NUCLEAR = Color(0, 255, 0, 100)
        private val COLOR_NUCLEAR_VEH_VISION = Color(180, 183, 76, 147)
        val LAYER_GENERIC = 4

        private val RESTRICTED_PP_DRAW = true
        private val DISABLED_PP_DRAW = false

        fun root(num: Double, root: Double): Double {
            return Math.pow(Math.E, Math.log(num) / root)
        }
    }

}
