import org.json.JSONObject
import java.io.*

/**
 * Обертка, позволяющая считывать из потока ввода и писать в поток вывода JSON-объекты
 * Для отладочных целей есть возможность читать команды не из STDIN, а из файла, например:
 *
 *
 * `gameMessage = JsonIO.readFromFile("messages.json")`
 */

object JsonIO {
    private var fileStream: FileInputStream? = null
    private var reader: InputStreamReader? = null
    private var bufferedReader: BufferedReader? = null

    fun readFromStdIn(): JSONObject? {
        return readFromStream(System.`in`)
    }

    fun readFromFile(fileName: String): JSONObject? {
        try {
            if (fileStream == null) {
                fileStream = FileInputStream(fileName)
            }
            return readFromStream(fileStream!!)
        } catch (e: FileNotFoundException) {
            return readFromStdIn()
        }

    }

    private fun readFromStream(stream: InputStream): JSONObject? {
        if (reader == null) {
            reader = InputStreamReader(stream)
        }

        if (bufferedReader == null) {
            bufferedReader = BufferedReader(reader!!)
        }

        try {
            val line = bufferedReader!!.readLine()
            return if (line != null && line.length != 0) {
                JSONObject(line)
            } else {
                null
            }
        } catch (e: IOException) {
            return null
        }

    }

    fun writeToStdOut(`object`: JSONObject) {
        writeToStream(System.out, `object`)
    }

    private fun writeToStream(stream: PrintStream, `object`: JSONObject) {
        stream.println(`object`.toString())
    }
}
