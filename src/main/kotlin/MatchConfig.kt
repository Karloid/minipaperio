import org.json.JSONObject

/**
 * Параметры матча (характеристики машин, контуры карт и т.д.), присылаемые в начале каждого матча.
 * Передается на вход обработчика `onMatchStarted` интерфейса [Strategy]
 */

var speed: Int = 0
var cell_width: Int = 0
var x_cells_count: Int = 0
var y_cells_count: Int = 0

class MatchConfig(root: JSONObject) {
    init {
        val realParams = root.getJSONObject("params")
        x_cells_count = realParams.getInt("x_cells_count")
        y_cells_count = realParams.getInt("y_cells_count")
        cell_width = realParams.getInt("width")
        speed = realParams.getInt("speed")
    }
}

val REQUEST_MAX_TIME = 5_000
val MAX_EXECUTION_TIME = 120_000