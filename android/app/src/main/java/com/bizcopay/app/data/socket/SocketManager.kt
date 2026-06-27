package com.bizcopay.app.data.socket

import io.socket.client.IO
import io.socket.client.Socket
import org.json.JSONObject

class SocketManager(private val token: String) {
    private lateinit var socket: Socket

    fun connect(serverUrl: String = com.bizcopay.app.data.network.NetworkConfig.SERVER_URL) {
        val options = IO.Options.builder()
            .setAuth(mapOf("token" to token))
            .build()
        socket = IO.socket(serverUrl, options)
        socket.connect()
    }

    fun joinTransaction(transactionId: String) {
        socket.emit("join:transaction", transactionId)
    }

    fun onPaymentApproved(callback: (JSONObject) -> Unit) {
        socket.on("payment:approved") { args -> callback(args[0] as JSONObject) }
    }

    fun onPaymentFailed(callback: (JSONObject) -> Unit) {
        socket.on("payment:failed") { args -> callback(args[0] as JSONObject) }
    }

    fun onPaymentPendingPin(callback: (JSONObject) -> Unit) {
        socket.on("payment:pending_pin") { args -> callback(args[0] as JSONObject) }
    }

    fun onWalletToppedUp(callback: (JSONObject) -> Unit) {
        socket.on("wallet:topped_up") { args -> callback(args[0] as JSONObject) }
    }

    fun onPaymentReceived(callback: (JSONObject) -> Unit) {
        socket.on("payment:received") { args -> callback(args[0] as JSONObject) }
    }

    fun disconnect() {
        if (::socket.isInitialized) socket.disconnect()
    }
}
