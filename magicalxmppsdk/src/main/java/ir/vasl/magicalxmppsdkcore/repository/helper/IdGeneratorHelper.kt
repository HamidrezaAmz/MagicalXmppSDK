package ir.vasl.magicalxmppsdkcore.repository.helper

class IdGeneratorHelper {

    companion object {

        private const val length = 10

        fun getRandomId(): String {
            val allowedChars = ('A'..'Z') + ('a'..'z') + ('0'..'9')
            return (1..length)
                .map { allowedChars.random() }
                .joinToString("")
        }
    }

}