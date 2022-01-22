package ir.vasl.magicalxmppsdkcore.repository.model

data class MagicalOutgoingMessage(
    val id: String,
    val message: String? = "No Message Found!",
    val from: String? = "No Sender Found!",
    val to: String? = "No Target Found!"
) {
    override fun toString(): String {
        return "\n id: $id | message: $message | from: $from | to: $to"
    }
}
