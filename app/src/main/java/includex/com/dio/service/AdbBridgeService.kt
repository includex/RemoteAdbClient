package includex.com.dio.service

import android.content.Intent
import android.os.IBinder
import android.util.Log
import includex.com.dio.consts.Consts
import java.io.DataInputStream
import java.io.IOException
import java.net.InetSocketAddress
import java.net.Socket

class AdbBridgeService : BaseService() {
    companion object {
        const val SOCKET_TIMEOUT = 3000
        const val LOOPBACK_IP_ADDRESS = "127.0.0.1"
        const val LOOPBACK_PORT = 8080

        const val RELAY_PORT = 7070
    }

    private lateinit var adbSocket: Socket
    private lateinit var relaySocket: Socket

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        AdbSocketThread().start()
        RelaySocketThread().start()

        return super.onStartCommand(intent, flags, startId)
    }

    override fun onBind(intent: Intent?): IBinder {
        throw RuntimeException()
    }

    inner class AdbSocketThread : Thread() {
        override fun run() {
            adbSocket = Socket()
            val bytes = ByteArray(1024)

            val adbAddress = InetSocketAddress(LOOPBACK_IP_ADDRESS, LOOPBACK_PORT)
            try {
                adbSocket.connect(adbAddress, SOCKET_TIMEOUT)
                Log.e("suc", "connect")

                while (true) {
                    val input = DataInputStream(adbSocket.getInputStream())
                    val count = input.read(bytes)
                    if (count <= 0) {
                        continue
                    }
                    Log.e("suc", "send")
                    relaySocket.getOutputStream().write(bytes, 0, count)
                }

            } catch (e: IOException) {
                Log.e("error adb", e.toString())
                relaySocket?.close()
                adbSocket.close()
            }

        }
    }

    inner class RelaySocketThread : Thread() {

        override fun run() {
            relaySocket = Socket()
            val bytes = ByteArray(1024)

            val relayAddress = InetSocketAddress(Consts.RELAY_IP, Consts.RELAY_PORT)
            try {
                relaySocket.connect(relayAddress)
                Log.e("relay suc", "connect")
                while (true) {
                    val input = DataInputStream(relaySocket.getInputStream())
                    val count = input.read(bytes)
                    if (count <= 0) {
                        continue
                    }
                    Log.e("relay suc", "send")
                    adbSocket.getOutputStream().write(bytes, 0, count)
                }
            } catch (e: IOException) {
                Log.e("relay error server", e.toString())
                relaySocket?.close()
                adbSocket.close()
            }
        }
    }
}