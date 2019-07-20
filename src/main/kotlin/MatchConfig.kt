import org.json.JSONArray
import org.json.JSONObject

/**
 * Параметры матча (характеристики машин, контуры карт и т.д.), присылаемые в начале каждого матча.
 * Передается на вход обработчика `onMatchStarted` интерфейса [Strategy]
 */

class MatchConfig(params: JSONObject) {
    //todo добавить нужные поля и классы и реализовать десериализацию json-объекта
    var myLives: Int = 0
    var enemyLives: Int = 0

    val carId: Int
    val carType: CarType


    var mapId: Int
    val mapType: MapType

    var buttonPoly = ArrayList<Point2D>()

    init {
        myLives = params.getInt("my_lives")
        enemyLives = params.getInt("enemy_lives")
        val protoCar = params.getJSONObject("proto_car")
        carId = protoCar.getInt("external_id")
        carType = CarType.values().first { it.id == carId }

        buttonPoly.clear()
        val buttonPolyJson = protoCar.getJSONArray("button_poly")
        for (o in buttonPolyJson) {
            if (o is JSONArray) {
                buttonPoly.add(Point2D(o.getInt(0), o.getInt(1)))
            }
        }

        mapId = params.getJSONObject("proto_map").getInt("external_id")
        mapType = MapType.values().first { it.id == mapId }
        // ...
    }
}

enum class CarType(val id: kotlin.Int) {
    Buggy(1),
    Bus(2),
    SquareWheelsBuggy(3),;
}

enum class MapType(val id: kotlin.Int) {
    PillMap(1),
    PillHubbleMap(2),
    PillHillMap(3),
    PillCarcassMap(4),
    IslandMap(5),
    IslandHoleMap(6), ;
}

/**
 * Состояние мира, присылаемое сервером на каждом тике.
 * Передается на вход обработчика `onNextTick` интерфейса [Strategy]
 */

val MAP_WIDTH: Int = 1200
val MAP_HEIGHT: Int = 800