package jsondatabase.client

import jsondatabase.Request
import java.io.DataInputStream
import java.io.DataOutputStream
import java.net.InetAddress
import java.net.Socket

fun main(args: Array<String>) {
    val client = Client("127.0.0.1", 23456)

    client.send(Request.handleRequest(args))
    client.receive()
    client.close()
}

class Client(address: String, port: Int) {
    private val socket = Socket(InetAddress.getByName(address), port).also { println("Client started!") }
    private val input = DataInputStream(socket.getInputStream())
    private val output = DataOutputStream(socket.getOutputStream())


    fun send(msg: String) {
        output.writeUTF(msg)
        println("Sent: $msg")
    }

    fun receive(): String {
        val receivedMsg = input.readUTF()
        println("Received: $receivedMsg")
        return receivedMsg
    }

    fun close() = socket.close()
}