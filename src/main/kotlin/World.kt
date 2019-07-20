import org.json.JSONObject

class World(params: JSONObject) {


    var tick: Int

    lateinit var me: Player

    init {
        tick = params.getInt("tick_num")
        val jsonPlayers = params.getJSONObject("players")

        val enPlayers = mutableListOf<Player>()
        jsonPlayers.keys().forEach {
            val p = Player(it, jsonPlayers.getJSONObject(it))
            if (it == "i") {
                enPlayers.add(p)
            } else {
                me = p
            }
        }
    }

    fun processPre(prevWorld: World) {

    }

}