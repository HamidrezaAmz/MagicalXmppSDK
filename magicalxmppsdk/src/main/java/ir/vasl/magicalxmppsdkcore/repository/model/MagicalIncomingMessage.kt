package ir.vasl.magicalxmppsdkcore.repository.model

data class MagicalIncomingMessage(val id: String, val message: String, val from: String) {

    override fun toString(): String {
        return "\n id: $id | message: $message | from: $from"
    }
}
