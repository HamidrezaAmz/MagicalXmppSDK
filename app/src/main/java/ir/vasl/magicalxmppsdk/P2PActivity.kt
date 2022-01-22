package ir.vasl.magicalxmppsdk

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import ir.vasl.magicalxmppsdk.databinding.ActivityP2pBinding
import ir.vasl.magicalxmppsdk.repository.PublicValue
import ir.vasl.magicalxmppsdkcore.MagicalXmppSDKCore
import ir.vasl.magicalxmppsdkcore.repository.enum.ConnectionStatus
import ir.vasl.magicalxmppsdkcore.repository.enum.NetworkStatus
import ir.vasl.magicalxmppsdkcore.repository.globalInterface.MagicalXmppSDKInterface
import ir.vasl.magicalxmppsdkcore.repository.helper.IdGeneratorHelper
import ir.vasl.magicalxmppsdkcore.repository.model.MagicalIncomingMessage
import ir.vasl.magicalxmppsdkcore.repository.model.MagicalOutgoingMessage

class P2PActivity : AppCompatActivity(), MagicalXmppSDKInterface {

    private val TAG = "MainActivity"

    private lateinit var binding: ActivityP2pBinding
    private lateinit var magicalXmppSDKInstance: MagicalXmppSDKCore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityP2pBinding.inflate(layoutInflater)
        setContentView(binding.root)
        title = "P2PActivity"

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
        binding.buttonChatHistory.setOnClickListener {
            getMessageHistory()
        }

        initializeXMPPSDK()
    }

    override fun onNetworkStatusChanged(networkStatus: NetworkStatus) {
        binding.textViewNetworkStatus.text = "Network Status: ${networkStatus.value}"
    }

    override fun onConnectionStatusChanged(connectionStatus: ConnectionStatus) {
        binding.textViewConnectionStatus.text = "Connection Status: ${connectionStatus.value}"
        if (connectionStatus == ConnectionStatus.AUTHENTICATED)
            magicalXmppSDKInstance.getMessageHistory(PublicValue.TEST_TARGET_USERNAME)
    }

    override fun onNewIncomingMessage(magicalIncomingMessage: MagicalIncomingMessage) {
        addItemIntoBoard("Incoming: $magicalIncomingMessage")
    }

    override fun onNewIncomingMessageHistory(magicalIncomingMessageHistoryList: List<MagicalIncomingMessage>) {
        Log.i(TAG, "onNewIncomingMessageHistory: size -> ${magicalIncomingMessageHistoryList.size}")
        Log.i(TAG, "onNewIncomingMessageHistory: list -> ${magicalIncomingMessageHistoryList.toString()}")

        addItemIntoBoard("IncomingMessageHistory: ${magicalIncomingMessageHistoryList.size}")
    }

    override fun onNewOutgoingMessage(magicalOutgoingMessage: MagicalOutgoingMessage) {
        addItemIntoBoard("Outgoing: $magicalOutgoingMessage")
    }

    private fun initializeXMPPSDK() {
        magicalXmppSDKInstance = MagicalXmppSDKCore.Builder(this@P2PActivity)
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

    private fun getMessageHistory() {

        if (::magicalXmppSDKInstance.isInitialized.not())
            return

        magicalXmppSDKInstance.getMessageHistory(PublicValue.TEST_TARGET_USERNAME)
    }

    override fun onDestroy() {
        if (::magicalXmppSDKInstance.isInitialized)
            magicalXmppSDKInstance.disconnect()
        super.onDestroy()
    }

    private fun refreshView() {
        binding.textViewConnectionStatus.text = "Connection Status: Disconnect"
        binding.appCompatTextViewBoard.text = ""
    }

    private fun addItemIntoBoard(newMessage: String) {
        var boardData = binding.appCompatTextViewBoard.text.toString()
        boardData = '\n' + newMessage + '\n' + boardData
        binding.appCompatTextViewBoard.text = boardData
    }

}