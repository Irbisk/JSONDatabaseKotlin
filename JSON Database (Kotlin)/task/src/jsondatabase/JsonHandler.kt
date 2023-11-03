package jsondatabase

import kotlinx.serialization.json.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import java.io.File

@Serializable
class Request {
    var type = ""
    var key: JsonElement = JsonPrimitive("")
    var value: JsonElement = JsonPrimitive("")

    companion object {
        fun handleRequest(args: Array<String>): String {
            val jsonRequest = Request()
            if (args.contains("-in")) {
                var fileName = ""
                for (i in args.indices) {
                    if (args[i] == "-in") {
                        fileName = args[i + 1]
                    }
                }
                val file = File("C:\\Users\\Кирилл\\IdeaProjects\\JSON Database (Kotlin)\\JSON Database (Kotlin)\\task\\src\\jsondatabase\\client\\data\\$fileName")
                return file.readText()

            } else {
                for (i in args.indices) {
                    if (args[i] == "-t") jsonRequest.type = args[i + 1]
                    if (args[i] == "-k") jsonRequest.key = JsonPrimitive(args[i + 1])
                    if (args[i] == "-v") {
                        jsonRequest.value = JsonPrimitive(args
                            .filterIndexed { index, s -> index > i }
                            .joinToString(" "))
                        break
                    }
                }
                return jsonRequest.toJson()
            }
        }

        fun String.toRequest(): Request = Json.decodeFromString<Request>(this)
    }

    fun toJson(): String = Json.encodeToString(this)
}

fun setValue(key: JsonElement, value: JsonElement, map: MutableMap<String, Any>) {
    if (key is JsonPrimitive) {
        if (value is JsonPrimitive) map[key.content] = value.content
        else {
            map[key.content] = value
            println("Value = ")
            println(value)
        }
    } else if (key is JsonArray && key.size == 1) {
        val key2 = key[0].jsonPrimitive
        if (value is JsonPrimitive) map[key2.content] = value.content
        else map[key2.content] = value
    } else {
        val list = mutableListOf<JsonElement>()
        val keyArray = key as JsonArray
        for (element in keyArray) {
            list.add(element)
        }
        val first = list.removeFirst()
        if (map.containsKey(first.jsonPrimitive.content)) {
            setValue(JsonArray(list), value, map[first.jsonPrimitive.content] as MutableMap<String, Any>)
        } else {
            map[first.jsonPrimitive.content] = mutableMapOf<String, Any>()
            setValue(JsonArray(list), value, map[first.jsonPrimitive.content] as MutableMap<String, Any>)
        }
    }
}

fun deleteKey(key: JsonElement, innerMap: MutableMap<String, Any>): Boolean {
    if (key is JsonPrimitive) {
        return if (innerMap.containsKey(key.content)) {
            innerMap.remove(key.content)
            true
        } else false
    } else if (key is JsonArray && key.size == 1) {
        val key2 = key[0].jsonPrimitive
        return if (innerMap.containsKey(key2.content)) {
            innerMap.remove(key2.content)
            true
        } else false
    } else {
        val list = mutableListOf<JsonElement>()
        val keyArray = key as JsonArray
        for (element in keyArray) {
            list.add(element)
        }
        val first = list.first()
        list.removeFirst()
        if (innerMap.containsKey(first.jsonPrimitive.content)) {
            return deleteKey(JsonArray(list), innerMap[first.jsonPrimitive.content] as MutableMap<String, Any>)
        }
        return false
    }
}

fun getKey(key: JsonElement, innerMap: MutableMap<String, Any>): JsonElement? {
    if (key is JsonPrimitive) {
        return if (innerMap.containsKey(key.content)) {
            if (innerMap[key.content] is Map<*, *>) {
                (innerMap[key.content] as Map<*, *>).toJsonElement()
            } else JsonPrimitive(innerMap[key.content].toString())
        }
        else null
    } else if (key is JsonArray && key.size == 1) {
        val key2 = key[0].jsonPrimitive
        return if (innerMap.containsKey(key2.content))
            if (innerMap[key2.content] is Map<*, *>) {
                (innerMap[key2.content] as Map<*, *>).toJsonElement()
            } else JsonPrimitive(innerMap[key2.content].toString())
        else null
    } else {
        val list = mutableListOf<JsonElement>()
        val keyArray = key as JsonArray
        for (element in keyArray) {
            list.add(element)
        }
        val first = list.first()
        list.removeFirst()
        if (innerMap.containsKey(first.jsonPrimitive.content)) {
            return getKey(JsonArray(list), innerMap[first.jsonPrimitive.content] as MutableMap<String, Any>)
        }
        return null
    }
}

fun Map<*,*>.toJsonElement(): JsonElement {
    val map = mutableMapOf<String, JsonElement>()
    this.forEach { any, u ->
        val key = any as? String ?: return@forEach
        val value = u ?: return@forEach

        when(value) {
            is Map<*, *> ->{
                map[key] = (value).toJsonElement()
                println(value)
            }
            else -> map[key] = JsonPrimitive(value.toString().replace("\"".toRegex(),""))
        }
    }
    return JsonObject(map)
}

fun jsonToMap(jsonElement: JsonElement): Any {
    return when (jsonElement) {
        is JsonObject -> {
            val map = mutableMapOf<String, Any>()
            for (element in jsonElement) {
                map[element.key] = jsonToMap(element.value)
            }
            map
        }

        is JsonPrimitive -> {
            jsonElement.content
        }

        else -> ""
    }
}


@Serializable
class Response(var response: String = "", var value: JsonElement = JsonPrimitive(""), var reason: String = "") {

    fun toJson(): String = Json.encodeToString(this)
    fun String.toResponse(): Response = Json.decodeFromString<Response>(this)
}