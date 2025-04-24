package es.diaryCMP.repositoriesModule.repositories

import es.diaryCMP.utilsModule.utils.encription.UserEncryption
import es.diaryCMP.utilsModule.utils.encription.decrypt
import es.diaryCMP.utilsModule.utils.encription.encrypt
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

interface GenericRepository {
    val userRepository: UserRepository

    fun getChild(): List<String>

    fun getEntryName(data: Any? = null): String

    private fun insertSalt(salt: ByteArray, cypher: ByteArray): ByteArray {
        if (salt.size != 16) {
            throw IllegalArgumentException("Salt must be 16 bytes long")
        }

        return (salt + cypher)
    }

    private fun getSalt(saltCipher: ByteArray): ByteArray {
        return saltCipher.copyOfRange(0, 16)
    }

    private fun getCipher(saltCipher: ByteArray): ByteArray {
        return saltCipher.copyOfRange(16, saltCipher.size)
    }

    suspend fun getEncryptedJson(json: String): String {
        val salt = UserEncryption.generateRandom16ByteArray()
        return Json.encodeToString(
            insertSalt(
                salt = salt,
                cypher = encrypt(
                    plainText = json,
                    key = getLocalKey(getSalt(salt))
                )
            )
        )
    }

    suspend fun getDecryptedFromJson(json: String): String {
        val byteArray = Json.decodeFromString<ByteArray>(json)
        return decrypt(cipherText = getCipher(byteArray), key = getLocalKey(getSalt(byteArray)))
    }

    private suspend fun getLocalKey(salt: ByteArray): ByteArray {
        return UserEncryption.getLocalKey(
            userKey = getUserKey(),
            salt = salt
        )
    }

    private suspend fun getUserKey(): ByteArray {
        return userRepository.localGetUserKey()!!
    }
}