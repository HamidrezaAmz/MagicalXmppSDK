package ir.vasl.magicalxmppsdk.repository.model

data class MagicalOutgoingMessage(
    val id: String,
    val message: String? = "No Message Found!",
    val from: String? = "No Sender Found!",
    val to: String? = "No Target Found!"
)
