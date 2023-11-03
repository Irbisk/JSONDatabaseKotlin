package jsondatabase.server

import jsondatabase.*
import jsondatabase.Request.Companion.toRequest
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.File
import java.net.InetAddress
import java.net.ServerSocket
import java.net.Socket
import java.nio.file.Paths
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.concurrent.locks.Lock
import java.util.concurrent.locks.ReadWriteLock
import java.util.concurrent.locks.ReentrantReadWriteLock

val db = File("C:\\Users\\Кирилл\\IdeaProjects\\JSON Database (Kotlin)\\JSON Database (Kotlin)\\task\\src\\jsondatabase\\server\\data\\db.json")

fun main() {

    println("Server started!")
    if (!db.exists()) {
        db.createNewFile()

    }
    val server = Server("127.0.0.1", 23456)
    server.getClient()

}



class Server(address: String, port: Int) {
    private val server = ServerSocket(port, 50, InetAddress.getByName(address))
    private lateinit var socket: Socket
    private lateinit var input: DataInputStream
    private lateinit var output: DataOutputStream
    private var isOn = true
    private var map = emptyMap<String, Any>().toMutableMap()
    private val lock: ReadWriteLock = ReentrantReadWriteLock()
    val readLock: Lock = lock.readLock()
    val writeLock: Lock = lock.writeLock()
    val executor = Executors.newSingleThreadExecutor()

    fun getClient() {

        server.use {
            while (true) {
                executor.submit {
                    socket = server.accept()
                    input = DataInputStream(socket.getInputStream())
                    input.use {
                        output = DataOutputStream(socket.getOutputStream())
                        output.use {
                            val receivedMsg = receive()
                            handleCommand(receivedMsg)
                            socket.close()
                        }
                    }
                }
                executor.awaitTermination(100, TimeUnit.MILLISECONDS)
            }
        }
    }


    private fun receive(): String {
        return input.readUTF()
    }

    private fun send(msg: String) {
        output.writeUTF(msg)
    }

    private fun handleCommand(msg: String) {
        val request = msg.toRequest()
        when (request.type) {
            "get" -> send(get(request.key).toJson())
            "set" -> send(set(request.key, request.value).toJson())
            "delete" -> send(delete(request.key).toJson())
            "exit" -> {
                send(Response("OK").toJson())
                isOn = false
                server.close()
                executor.shutdown()
            }
        }
    }

    fun get(key: JsonElement): Response {
        readDB()
        val result = getKey(key, map)
        return if (result != null) Response("OK", result)
        else Response("ERROR", reason = "No such key")
    }


    fun set(key: JsonElement, value: JsonElement): Response {
        readDB()
        setValue(key, value, map)
        writeDB()
        return Response("OK")
    }

    fun delete(key: JsonElement): Response {
        readDB()
        val result = deleteKey(key, map)
        return if (result) {
            writeDB()
            Response("OK")
        } else Response("ERROR", reason = "No such key")
    }

    private fun readDB() {
        map.clear()
        readLock.lock()
        val text = db.readText()
        readLock.unlock()
        if (text.isNotEmpty()) {
            map = jsonToMap(Json.parseToJsonElement(text)) as MutableMap<String, Any>
        }
    }

    private fun writeDB() {
        writeLock.lock()
        db.writeText(map.toJsonElement().toString())
        println("WriteDB2")
        writeLock.unlock()
    }
}