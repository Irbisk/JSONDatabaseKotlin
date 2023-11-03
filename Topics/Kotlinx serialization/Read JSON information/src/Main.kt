import kotlinx.serialization.*
import kotlinx.serialization.json.*

@Serializable
data class City(val name: String, val population: Int)

fun main() {
    val jsonString = readln()

    val city = Json.decodeFromString<City>(jsonString)

    println("${city.name} ${city.population}")
}