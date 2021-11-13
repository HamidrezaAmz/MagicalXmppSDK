package ir.vasl.magicalxmppsdkcore

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import ir.vasl.magicalxmppsdkcore.databinding.ActivityMainBinding
import ir.vasl.magicalxmppsdkcore.repository.PublicValue
import ir.vasl.magicalxmppsdkcore.repository.enum.ConnectionStatus
import ir.vasl.magicalxmppsdkcore.repository.enum.NetworkStatus
import ir.vasl.magicalxmppsdkcore.repository.globalInterface.MagicalXmppSDKInterface
import ir.vasl.magicalxmppsdkcore.repository.helper.IdGeneratorHelper
import ir.vasl.magicalxmppsdkcore.repository.model.MagicalIncomingMessage
import ir.vasl.magicalxmppsdkcore.repository.model.MagicalOutgoingMessage

class MainActivity : AppCompatActivity(), MagicalXmppSDKInterface {

    private val TAG = "MainActivity"
    private lateinit var binding: ActivityMainBinding
    private lateinit var magicalXmppSDKInstance: MagicalXmppSDKCore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.buttonConnect.setOnClickListener {
            initializeXMPPSDK()
        }
        binding.buttonDisconnect.setOnClickListener {
            if (::magicalXmppSDKInstance.isInitialized) {
                magicalXmppSDKInstance.disconnect()
                refreshView()
            }
        }
        binding.buttonNewMessage.setOnClickListener {
            if (::magicalXmppSDKInstance.isInitialized &&
                magicalXmppSDKInstance.getConnectionStatus() == ConnectionStatus.AUTHENTICATED
            ) {
                sendNewMessage()
            }
        }

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
        magicalXmppSDKInstance = MagicalXmppSDKCore.Builder(this@MainActivity)
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
        if (::magicalXmppSDKInstance.isInitialized)
            magicalXmppSDKInstance.disconnect()
        super.onDestroy()
    }

    private fun refreshView() {
        binding.textViewConnectionStatus.text = "Disconnect"
        binding.textViewConnectionIncomingMessage.text = "Incoming: --- "
        binding.textViewConnectionOutgoingMessage.text = "Outgoing: ---"
    }


}