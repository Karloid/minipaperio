import org.json.JSONObject

/**
 * Параметры матча (характеристики машин, контуры карт и т.д.), присылаемые в начале каждого матча.
 * Передается на вход обработчика `onMatchStarted` интерфейса [Strategy]
 */

class MatchConfig(root: JSONObject) {

    var speed: Int
    var width: Int
    var x_cells_count: Int
    var y_cells_count: Int

    init {
        val realParams = root.getJSONObject("params")
        x_cells_count = realParams.getInt("x_cells_count")
        y_cells_count = realParams.getInt("y_cells_count")
        width = realParams.getInt("width")
        speed = realParams.getInt("speed")
    }
}

val REQUEST_MAX_TIME = 5_000
val MAX_EXECUTION_TIME = 120_000