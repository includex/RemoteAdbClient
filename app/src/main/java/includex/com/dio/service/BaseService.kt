package includex.com.dio.service

import android.app.Service
import android.content.Intent
import android.os.IBinder
import includex.com.dio.event.AllDisconnectEvent
import includex.com.dio.event.StatusChangedEvent
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

open class BaseService : Service() {

    lateinit var self : Service;

    override fun onCreate() {
        self = this

        EventBus.getDefault().register(this)
        super.onCreate()
    }

    override fun onDestroy() {
        EventBus.getDefault().unregister(this)
        super.onDestroy()
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEvent(e: AllDisconnectEvent) {
        self.stopSelf()
    }

    override fun onBind(p0: Intent?): IBinder {
        throw RuntimeException()
    }

}