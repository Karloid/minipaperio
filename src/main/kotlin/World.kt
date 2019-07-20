import org.json.JSONObject

class World(params: JSONObject) {

    var myCar: Car
    var enemyCar: Car
    var deadLine: Float = 0.toFloat()

    init {
        myCar = Car(params.getJSONArray("my_car"), this)
        enemyCar = Car(params.getJSONArray("enemy_car"), this)

        deadLine = params.getFloat("deadline_position")
    }

    fun processPre(prevWorld: World) {
        myCar.processPre(prevWorld.myCar)
        enemyCar.processPre(prevWorld.enemyCar)
    }

}