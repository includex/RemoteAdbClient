package includex.com.dio.service

import android.content.Intent
import android.util.Log
import com.google.gson.Gson
import includex.com.dio.consts.Consts
import includex.com.dio.consts.Protocol
import includex.com.dio.enum.StateType
import includex.com.dio.event.StatusChangedEvent
import includex.com.dio.model.ClientInformation
import includex.com.dio.singleton.LocalInformation
import org.greenrobot.eventbus.EventBus
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.io.PrintWriter
import java.net.InetSocketAddress
import java.net.Socket

class MainSocketService : BaseService() {

    companion object {
        var isRunning = false
    }

    private lateinit var socket: Socket

    private lateinit var address: InetSocketAddress

    private lateinit var socketThread: MainSocketThread


    override fun onCreate() {
        super.onCreate()

        isRunning = true
    }

    override fun onDestroy() {
        isRunning = false

        socketThread.interrupt()
        socket.close()
        super.onDestroy()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        socketThread = MainSocketThread().apply { start() }
        return super.onStartCommand(intent, flags, startId)
    }

    inner class MainSocketThread : Thread() {
        private lateinit var reader: BufferedReader

        private lateinit var writer: PrintWriter

        override fun run() {
            EventBus.getDefault().post(StatusChangedEvent(StateType.CONNECTING))

            socket = Socket()
            address = InetSocketAddress(Consts.API_SERVER_IP_ADDRESS, Consts.API_SERVER_PORT)
            try {
                socket.connect(address, Consts.SOCKET_TIMEOUT)

                reader = BufferedReader(InputStreamReader(socket.getInputStream()))
                writer = PrintWriter(socket.getOutputStream())
                while (true) {
                    var data = reader.readLine()
                    if (data != null) {
                        Log.e("=====", data)
                        onEvent(data)
                    }

                    if (isInterrupted) {
                        return;
                    }
                }

            } catch (exception: IOException) {
                Log.e("MainSocketService", exception.toString())
                EventBus.getDefault().post(StatusChangedEvent(StateType.ERROR_AND_DISCONNECTED))
                stopSelf()
            }
        }

        private fun onEvent(str: String) {
            when (str) {
                Protocol.S_WELCOME -> {
                    writer.print(Gson().toJson(ClientInformation(LocalInformation.code, LocalInformation.name)))
                    writer.flush()
                }

                Protocol.S_READY_AND_WAIT -> {
                    EventBus.getDefault().post(StatusChangedEvent(StateType.CONNECTED))
                }
            }
        }
    }
}