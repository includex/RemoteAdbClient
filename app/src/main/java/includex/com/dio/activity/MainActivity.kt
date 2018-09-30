package includex.com.dio.activity

import android.content.Intent
import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import includex.com.dio.R
import includex.com.dio.consts.Consts
import includex.com.dio.databinding.ActivityMainBinding
import includex.com.dio.enum.StateType
import includex.com.dio.event.StatusChangedEvent
import includex.com.dio.service.MainSocketService
import includex.com.dio.singleton.LocalInformation
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import android.view.inputmethod.InputMethodManager
import includex.com.dio.event.AllDisconnectEvent
import includex.com.dio.service.AdbBridgeService
import includex.com.dio.service.CanvasService


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        EventBus.getDefault().register(this)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        bindEvents()

        if (MainSocketService.isRunning) {
            changeStatus(StateType.READY, null)
        }
    }

    private fun bindEvents() {
        binding.btnConnect.setOnClickListener {
            val suc = Consts.checkRule(binding.etCode.text.toString(), binding.etName.text.toString())
            if (!suc) {
                binding.tvState.setText(R.string.invalid_code_or_name)
                return@setOnClickListener
            }

            LocalInformation.code = binding.etCode.text.toString()
            LocalInformation.name = binding.etName.text.toString()

            binding.btnConnect.isEnabled = false
            showLoadingIndicator()
            startService(Intent(this, MainSocketService::class.java))

            hideSoftInput()
        }

        binding.btnDisconnect.setOnClickListener {
            EventBus.getDefault().post(AllDisconnectEvent())
        }
    }

    fun hideSoftInput() {
        val imm = getSystemService(InputMethodManager::class.java)
        val view = getCurrentFocus()
        //If no view currently has focus, create a new one, just so we can grab a window token from it
        if (view == null) {
            return
        }

        imm.hideSoftInputFromWindow(view.getWindowToken(), 0)
    }

    private fun changeStatus(state: StateType, message: String?) {
        binding.etCode.isEnabled = true
        binding.etName.isEnabled = true

        when (state) {
            StateType.CONNECTING -> binding.tvState.setText(R.string.state_connecting)
            StateType.READY -> binding.tvState.setText(R.string.state_ready)
            StateType.CONNECTED -> {
                hideLoadingIndiccator()

                binding.etCode.isEnabled = false
                binding.etName.isEnabled = false

                binding.tvState.setText(R.string.state_connected)
                binding.btnConnect.visibility = View.GONE
                binding.btnDisconnect.visibility = View.VISIBLE

                startService(Intent(this, CanvasService::class.java))
                startService(Intent(this, AdbBridgeService::class.java))
            }
            StateType.DISCONNECTED -> {
                binding.tvState.setText(R.string.state_disconnected)

                binding.btnConnect.isEnabled = true
                binding.btnConnect.visibility = View.VISIBLE
                binding.btnDisconnect.visibility = View.GONE
            }
            StateType.ERROR_AND_DISCONNECTED -> {
                hideLoadingIndiccator()

                binding.btnConnect.isEnabled = true
                binding.btnConnect.visibility = View.VISIBLE
                binding.btnDisconnect.visibility = View.GONE

                binding.btnConnect.isEnabled = true
                message.let {
                    if (it != null) {
                        binding.tvState.setText(it)
                    } else {
                        binding.tvState.setText(R.string.state_error_and_disconnected)
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        EventBus.getDefault().unregister(this)
        super.onDestroy()
    }

    fun showLoadingIndicator() {
        binding.rlLoadingIndicator.visibility = View.VISIBLE
    }

    fun hideLoadingIndiccator() {
        binding.rlLoadingIndicator.visibility = View.GONE
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(e: StatusChangedEvent) {
        changeStatus(e.state, e.message)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEvent(e: AllDisconnectEvent) {
        changeStatus(StateType.READY, null)
    }
}
