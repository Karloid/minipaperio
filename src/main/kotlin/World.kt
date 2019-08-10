import org.json.JSONObject
import java.util.*
import kotlin.collections.HashMap

class World(params: JSONObject, val config: MatchConfig) {

    //player.id to steps
    private lateinit var access: HashMap<String, PlainArray<Int>>
    var tick: Int
    lateinit var cells: PlainArray<MapCell>

    lateinit var me: Player
    val enPlayers: MutableList<Player>
    lateinit var bonuses: List<Bonus>

    init {
        tick = params.getInt("tick_num")
        val jsonPlayers = params.getJSONObject("players")


        enPlayers = mutableListOf()
        jsonPlayers.keys().forEach {
            val p = Player(it, jsonPlayers.getJSONObject(it))
            if (it == "i") {
                me = p
            } else {
                enPlayers.add(p)
            }
        }
        val jsonBonuses = params.getJSONArray("bonuses")
        bonuses = List(jsonBonuses.count()) { Bonus(jsonBonuses.getJSONObject(it)) }
    }


    fun processPre(prevWorld: World) {
        this.cells = prevWorld.cells
    }

    fun calcMap() {
        //clear
        cells.fori { x, y, cell ->
            cell.lines = null
            cell.territory = null
        }

        (enPlayers + me).fori { p ->
            p.territory.fori {
                cells[it.x, it.y]!!.territory = p
            }
            p.lines.fori {
                cells[it.x, it.y]!!.lines = p
            }
        }

        access = HashMap()

        val allPlayers = enPlayers + me
        allPlayers.forEach {
            access[it.id] = calcAccess(it, allPlayers)

            if (it == me) {
                printDebugMyMap(access[it.id]!!)
            }
        }
    }

    private fun printDebugMyMap(plainArray: PlainArray<Int>) {
        repeat(y_cells_count) { y ->
            var string = ""
            repeat(x_cells_count) { x ->
                string += " " + plainArray.get(x, y)
            }
            MainKt.myDebugLog(string)
        }
    }

    fun createMap() {
        cells = PlainArray(x_cells_count, y_cells_count) { MapCell() }

        cells.fori { x, y, cell -> cell.pos = Point2D(x, y) }
    }

    private fun calcAccess(curPlayer: Player, allPlayers: List<Player>): PlainArray<Int> {
        val result = PlainArray(x_cells_count, y_cells_count) { Int.MAX_VALUE }

        result.setFast(curPlayer.pos, 0)
        val queueTest = LinkedList<Point2D>();
        queueTest.add(curPlayer.pos)

        while (true) {
            val el = queueTest.pollFirst() ?: break

            val adjacent = getAdjacent(el)

            val myVal = result.getFast(el)
            adjacent.forEach { candidate ->
                if (candidate.lines == curPlayer) {
                    return@forEach
                }
                val candidatePos = candidate.pos
                val candidateVal = result.getFast(candidatePos)
                if (candidateVal > myVal + 1) {
                    result.setFast(candidatePos, myVal + 1)
                    queueTest.add(candidatePos)
                }
            }
        }

        return result
    }

    fun getAdjacent(position: Point2D): MutableList<MapCell> {
        val result = mutableListOf<MapCell>()
        cells[position.x - 1, position.y]?.let { result.add(it) }
        cells[position.x, position.y - 1]?.let { result.add(it) }
        cells[position.x, position.y + 1]?.let { result.add(it) }
        cells[position.x + 1, position.y]?.let { result.add(it) }
        return result
    }
}