import kotlinx.serialization.*
import kotlinx.serialization.json.*

@Serializable
data class Employee(val name: String, val position: String)

fun main() {
    val (name, position) = readln().split(",")
    val employee = Employee(name, position)
    println(Json.encodeToString(employee))

}