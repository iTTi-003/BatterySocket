package com.battery.socket

import android.content.Context
import android.os.BatteryManager
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import rikka.shizuku.Shizuku
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.ServerSocket
import java.net.Socket

class MainActivity : AppCompatActivity() {
    companion object {
        private const val PORT = 43210
    }
    private var serverSocket: ServerSocket? = null
    private lateinit var statusText: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        statusText = findViewById(R.id.textView)

        if (!Shizuku.pingBinder()) {
            statusText.text = "Shizuku未连接！\n激活Shizuku后重启APP"
        } else {
            startServer()
            statusText.text = "服务运行中\n端口：$PORT\n发送命令 get\n返回：电压(mV),电流(uA)"
        }
    }

    private fun startServer() {
        Thread {
            try {
                serverSocket = ServerSocket(PORT)
                while (!Thread.currentThread().isInterrupted) {
                    val client = serverSocket!!.accept()
                    handleClient(client)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }.start()
    }

    private fun handleClient(socket: Socket) {
        try {
            val reader = BufferedReader(InputStreamReader(socket.inputStream))
            val writer = OutputStreamWriter(socket.outputStream)
            val cmd = reader.readLine()
            if (cmd == "get") {
                val bm = getSystemService(Context.BATTERY_SERVICE) as BatteryManager
                val voltage = bm.getIntProperty(BatteryManager.BATTERY_PROPERTY_VOLTAGE)
                val current = bm.getLongProperty(BatteryManager.BATTERY_PROPERTY_CURRENT_NOW)
                writer.write("$voltage,$current\n")
            }
            writer.flush()
        } catch (e: Exception) {
        } finally {
            socket.close()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        serverSocket?.close()
    }
}
