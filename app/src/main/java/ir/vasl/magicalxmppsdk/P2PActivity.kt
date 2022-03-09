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
import java.util.*

class P2PActivity : AppCompatActivity(), MagicalXmppSDKInterface {

    private val TAG = "MainActivity"

    private lateinit var binding: ActivityP2pBinding
    private lateinit var magicalXmppSDKInstance: MagicalXmppSDKCore

    private var messages: ArrayList<MagicalIncomingMessage> = arrayListOf()

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

        binding.buttonChatLastPageHistory.setOnClickListener {
            getMessageHistoryLastPage()
        }
        binding.buttonChatHistoryBeforeId.setOnClickListener {

            if (messages.size <= 0)
                return@setOnClickListener

            Log.i(TAG, "getMessageHistoryBeforeId: ${messages[messages.size - 1].message}")
            val uid = messages[messages.size - 1].id
            getMessageHistoryBeforeId(uid)
        }
        binding.buttonChatHistoryAfterId.setOnClickListener {

            if (messages.size <= 0)
                return@setOnClickListener

            Log.i(TAG, "getMessageHistoryAfterId: ${messages[0].message}")
            val uid = messages[messages.size - 1].id
            getMessageHistoryAfterId(uid)
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
        Log.i(TAG, "onNewIncomingMessage...")
        Log.i(TAG, "$magicalIncomingMessage")

        addItemIntoBoard("onNewIncomingMessage: $magicalIncomingMessage")
    }

    override fun onNewIncomingMessageHistory(magicalIncomingMessageHistoryList: MutableList<MagicalIncomingMessage>) {
        magicalIncomingMessageHistoryList.reverse()
        messages.addAll(magicalIncomingMessageHistoryList)

        Log.i(TAG, "onNewIncomingMessageHistory: size -> ${magicalIncomingMessageHistoryList.size}")
        Log.i(TAG, "onNewIncomingMessageHistory: list... ")

        for (magicalIncomingMessage in magicalIncomingMessageHistoryList) {
            Log.i(TAG, "onNewIncomingMessageHistory: $magicalIncomingMessage")
            addItemIntoBoard("Message: ${magicalIncomingMessage.message}")
        }

        addItemIntoBoard("IncomingMessageHistory size: ${magicalIncomingMessageHistoryList.size}")
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

    private fun sendNewMessage(newMessageCode: String? = null) {

        if (!::magicalXmppSDKInstance.isInitialized)
            return

        val messageCode = newMessageCode ?: IdGeneratorHelper.getRandomId()
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

    private fun getMessageHistoryLastPage() {
        if (::magicalXmppSDKInstance.isInitialized.not())
            return

        magicalXmppSDKInstance.getMessageHistoryLastPage(PublicValue.TEST_TARGET_USERNAME)
    }

    private fun getMessageHistoryBeforeId(Uid: String) {
        if (::magicalXmppSDKInstance.isInitialized.not())
            return

        magicalXmppSDKInstance.getMessageHistoryBeforeId(PublicValue.TEST_TARGET_USERNAME, Uid)
    }

    private fun getMessageHistoryAfterId(Uid: String) {
        if (::magicalXmppSDKInstance.isInitialized.not())
            return

        magicalXmppSDKInstance.getMessageHistoryAfterId(PublicValue.TEST_TARGET_USERNAME, Uid)
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