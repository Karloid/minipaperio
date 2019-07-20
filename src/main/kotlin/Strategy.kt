/**
 * Интерфейс типового робота, получающего на вход конфигурацию и состояние мира в формате JSON,
 * и отправляющего на выход своё решение на текущем шаге (тоже в JSON) *
 */
interface Strategy {
    fun onMatchStarted(matchConfig: MatchConfig)
    fun onNextTick(world: World, move: Move)
    fun onParsingError(message: String)
}
