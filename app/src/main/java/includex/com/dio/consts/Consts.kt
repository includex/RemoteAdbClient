package includex.com.dio.consts

import android.text.TextUtils
import java.util.regex.Pattern

object Consts {
    fun checkRule(code: String, name: String): Boolean {
        if (TextUtils.isEmpty(code) || TextUtils.isEmpty(name)) {
            return false
        }

        if (!TextUtils.isDigitsOnly(code)) {
            return false
        }

        return Pattern.matches(".*[a-zA-Z]+.*", name)
    }

    val API_SERVER_IP_ADDRESS = "39.118.171.34"
    val API_SERVER_PORT = 9090
    val SOCKET_TIMEOUT = 3000
    val RELAY_PORT = 8090
    val RELAY_IP = "39.118.171.34"
}