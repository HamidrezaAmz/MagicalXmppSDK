package ir.vasl.magicalxmppsdk

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import ir.vasl.magicalxmppsdk.databinding.ActivityMainBinding
import ir.vasl.magicalxmppsdk.repository.PublicValue
import ir.vasl.magicalxmppsdk.repository.`interface`.MagicalXmppSDKInterface
import ir.vasl.magicalxmppsdk.repository.enum.ConnectionStatus
import ir.vasl.magicalxmppsdk.repository.enum.NetworkStatus
import ir.vasl.magicalxmppsdk.repository.helper.IdGeneratorHelper
import ir.vasl.magicalxmppsdk.repository.model.MagicalIncomingMessage
import ir.vasl.magicalxmppsdk.repository.model.MagicalOutgoingMessage

class MainActivity : AppCompatActivity(), MagicalXmppSDKInterface, View.OnClickListener {

    private val TAG = "MainActivity"
    private lateinit var binding: ActivityMainBinding
    private lateinit var magicalXmppSDKInstance: MagicalXmppSDK

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.buttonConnect.setOnClickListener(this)
        binding.buttonDisconnect.setOnClickListener(this)
        binding.buttonNewMessage.setOnClickListener(this)

        initializeXMPPSDK()
    }

    override fun onNetworkStatusChanged(networkStatus: NetworkStatus) {
        binding.textViewNetworkStatus.text = "Network Status: ${networkStatus.value}"
    }

    override fun onConnectionStatusChanged(connectionStatus: ConnectionStatus) {
        binding.textViewConnectionStatus.text = "Connection Status: ${connectionStatus.value}"
    }

    override fun onNewIncomingMessage(magicalIncomingMessage: MagicalIncomingMessage) {
        binding.textViewConnectionIncomingMessage.text =
            "Incoming: ${magicalIncomingMessage.message}"
    }

    override fun onNewOutgoingMessage(magicalOutgoingMessage: MagicalOutgoingMessage) {
        binding.textViewConnectionOutgoingMessage.text =
            "Outgoing: ${magicalOutgoingMessage.message}"
    }

    private fun initializeXMPPSDK() {
        magicalXmppSDKInstance = MagicalXmppSDK.Builder(this@MainActivity)
            .setUsername(PublicValue.TEST_USERNAME)
            .setPassword(PublicValue.TEST_PASSWORD)
            .setDomain(PublicValue.TEST_DOMAIN)
            .setHost(PublicValue.TEST_HOST)
            .setPort(PublicValue.TEST_PORT)
            .setCallback(this)
            .build()
    }

    private fun sendNewMessage() {

        if (!::magicalXmppSDKInstance.isInitialized)
            return

        val messageCode = IdGeneratorHelper.getRandomId()
        magicalXmppSDKInstance.sendNewMessage(
            MagicalOutgoingMessage(
                id = IdGeneratorHelper.getRandomId(),
                message = "Message: $messageCode",
                from = PublicValue.TEST_USERNAME,
                to = PublicValue.TEST_TARGET_USERNAME
            )
        )
    }

    override fun onDestroy() {
        super.onDestroy()
        if (::magicalXmppSDKInstance.isInitialized)
            magicalXmppSDKInstance.disconnect()
    }

    override fun onClick(view: View?) {
        when (view?.id) {
            R.id.button_connect -> {
                initializeXMPPSDK()
            }
            R.id.button_disconnect -> {
                if (::magicalXmppSDKInstance.isInitialized)
                    magicalXmppSDKInstance.disconnect()
                refreshView()
            }
            R.id.button_new_message -> {
                if (::magicalXmppSDKInstance.isInitialized &&
                    magicalXmppSDKInstance.getConnectionStatus() == ConnectionStatus.AUTHENTICATED
                ) {
                    sendNewMessage()
                } else {
                    Log.i(TAG, "onClick: ${magicalXmppSDKInstance.getConnectionStatus().name}.")
                }
            }
        }
    }

    fun refreshView() {
        binding.textViewConnectionStatus.text = "Disconnect"
        binding.textViewConnectionIncomingMessage.text = "Incoming: --- "
        binding.textViewConnectionOutgoingMessage.text = "Outgoing: ---"
    }


}