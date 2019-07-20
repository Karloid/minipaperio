import org.json.JSONObject

class World(params: JSONObject, val config: MatchConfig) {

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
                enPlayers.add(p)
            } else {
                me = p
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
    }

    fun createMap() {
        cells = PlainArray(x_cells_count, y_cells_count) { MapCell() }

        cells.fori { x, y, cell -> cell.pos = Point2D(x, y) }
    }
}