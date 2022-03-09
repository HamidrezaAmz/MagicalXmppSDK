package ir.vasl.magicalxmppsdk

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import ir.vasl.magicalxmppsdk.databinding.ActivityMucBinding
import ir.vasl.magicalxmppsdk.repository.PublicValue
import ir.vasl.magicalxmppsdk.repository.PublicValue.Companion.TAG
import ir.vasl.magicalxmppsdkcore.MagicalXmppSDKMucCore
import ir.vasl.magicalxmppsdkcore.repository.enum.ConnectionStatus
import ir.vasl.magicalxmppsdkcore.repository.enum.NetworkStatus
import ir.vasl.magicalxmppsdkcore.repository.globalInterface.MagicalXmppSDKInterface
import ir.vasl.magicalxmppsdkcore.repository.helper.IdGeneratorHelper
import ir.vasl.magicalxmppsdkcore.repository.model.MagicalIncomingMessage
import ir.vasl.magicalxmppsdkcore.repository.model.MagicalOutgoingMessage

class MucActivity : AppCompatActivity(), MagicalXmppSDKInterface {

    private lateinit var binding: ActivityMucBinding
    private lateinit var magicalXmppSDKMucCoreInstance: MagicalXmppSDKMucCore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMucBinding.inflate(layoutInflater)
        setContentView(binding.root)
        title = "MultiUserChatActivity"

        binding.buttonConnect.setOnClickListener {
            initializeXMPPSDK()
        }
        binding.buttonDisconnect.setOnClickListener {
            if (::magicalXmppSDKMucCoreInstance.isInitialized) {
                magicalXmppSDKMucCoreInstance.disconnect()
                refreshView()
            }
        }
        binding.buttonNewMessage.setOnClickListener {
            if (::magicalXmppSDKMucCoreInstance.isInitialized &&
                magicalXmppSDKMucCoreInstance.getConnectionStatus() == ConnectionStatus.AUTHENTICATED
            ) {
                sendNewMessage()
            }
        }
        binding.buttonChatHistory.setOnClickListener {
            getMessageHistory()
        }

        initializeXMPPSDK()
    }

    private fun sendNewMessage() {

        if (!::magicalXmppSDKMucCoreInstance.isInitialized)
            return

        val messageCode = IdGeneratorHelper.getRandomId()
        magicalXmppSDKMucCoreInstance.sendNewMessage(
            MagicalOutgoingMessage(
                id = IdGeneratorHelper.getRandomId(),
                message = "Message: $messageCode",
                from = PublicValue.TEST_USERNAME,
                to = PublicValue.TEST_TARGET_USERNAME
            )
        )
    }

    private fun getMessageHistory() {

        if (::magicalXmppSDKMucCoreInstance.isInitialized.not())
            return

        magicalXmppSDKMucCoreInstance.getChatHistory(PublicValue.TEST_ROOM)
    }

    override fun onNetworkStatusChanged(networkStatus: NetworkStatus) {
        binding.textViewNetworkStatus.text = "Network Status: ${networkStatus.value}"
    }

    override fun onConnectionStatusChanged(connectionStatus: ConnectionStatus) {
        binding.textViewConnectionStatus.text = "Connection Status: ${connectionStatus.value}"

        if (connectionStatus == ConnectionStatus.AUTHENTICATED) {
            magicalXmppSDKMucCoreInstance.getChatHistory(PublicValue.TEST_TARGET_USERNAME)
        }
    }

    override fun onNewIncomingMessage(magicalIncomingMessage: MagicalIncomingMessage) {
        addItemIntoBoard("Incoming: $magicalIncomingMessage")
    }

    override fun onNewIncomingMessageHistory(magicalIncomingMessageHistoryList: MutableList<MagicalIncomingMessage>) {
        Log.i(TAG, "onNewIncomingMessageHistory: size -> ${magicalIncomingMessageHistoryList.size}")
    }

    override fun onNewOutgoingMessage(magicalOutgoingMessage: MagicalOutgoingMessage) {
        addItemIntoBoard("Outgoing: $magicalOutgoingMessage")
    }

    private fun initializeXMPPSDK() {
        magicalXmppSDKMucCoreInstance = MagicalXmppSDKMucCore.Builder(this@MucActivity)
            .setUsername(PublicValue.TEST_USERNAME)
            .setPassword(PublicValue.TEST_PASSWORD)
            .setDomain(PublicValue.TEST_DOMAIN)
            .setHost(PublicValue.TEST_HOST)
            .setPort(PublicValue.TEST_PORT)
            .setRoom(PublicValue.TEST_ROOM)
            .setCallback(this)
            .build()
    }

    override fun onDestroy() {
        if (::magicalXmppSDKMucCoreInstance.isInitialized)
            magicalXmppSDKMucCoreInstance.disconnect()
        super.onDestroy()
    }

    private fun refreshView() {
        binding.textViewConnectionStatus.text = "Disconnect"
        binding.appCompatTextViewBoard.text = ""
    }

    private fun addItemIntoBoard(newMessage: String) {
        var boardData = binding.appCompatTextViewBoard.text.toString()
        boardData = '\n' + newMessage + '\n' + boardData
        binding.appCompatTextViewBoard.text = boardData
    }
}