package ir.vasl.magicalxmppsdk

import android.os.Bundle
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

    private lateinit var binding: ActivityMainBinding
    private lateinit var magicalXmppSDKInstance: MagicalXmppSDK

    private lateinit var adapter: MessageAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.buttonSendNewMessage.setOnClickListener(this)

        magicalXmppSDKInstance = MagicalXmppSDK.Builder(this@MainActivity)
            .setUsername(PublicValue.TEST_USERNAME)
            .setPassword(PublicValue.TEST_PASSWORD)
            .setDomain(PublicValue.TEST_DOMAIN)
            .setHost(PublicValue.TEST_HOST)
            .setPort(PublicValue.TEST_PORT)
            .setCallback(this)
            .build()

        adapter = MessageAdapter()
        binding.rvMessages.adapter = adapter
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.button_send_new_message -> {
                sendNewMessage()
            }
        }
    }

    override fun onNetworkStatusChanged(networkStatus: NetworkStatus) {
        binding.textViewNetworkStatus.text = "Network Status: ${networkStatus.value}"
    }

    override fun onConnectionStatusChanged(connectionStatus: ConnectionStatus) {
        binding.textViewConnectionStatus.text = "Connection Status: ${connectionStatus.value}"
    }

    override fun onNewIncomingMessage(magicalIncomingMessage: MagicalIncomingMessage) {
        binding.textViewConnectionIncomingMessage.text =
            "Incoming Message: ${magicalIncomingMessage.message}"
        var messages :ArrayList<MagicalIncomingMessage> = adapter.currentList as ArrayList<MagicalIncomingMessage>
        messages.add(magicalIncomingMessage)
//        adapter.submitList(messages as List<MagicalIncomingMessage>)
    }

    override fun onNewOutgoingMessage(magicalOutgoingMessage: MagicalOutgoingMessage) {
        binding.textViewConnectionOutgoingMessage.text =
            "Outgoing Message: ${magicalOutgoingMessage.message}"
    }

    private fun sendNewMessage() {

        val messageCode = IdGeneratorHelper.getRandomId()

        magicalXmppSDKInstance.sendNewMessage(
            MagicalOutgoingMessage(
                id = IdGeneratorHelper.getRandomId(),
                message = "NEW MESSAGE: $messageCode",
                from = PublicValue.TEST_USERNAME,
                to = PublicValue.TEST_TARGET_USERNAME
            )
        )
    }

    override fun onDestroy() {
        super.onDestroy()
        magicalXmppSDKInstance.disconnect()
    }

}