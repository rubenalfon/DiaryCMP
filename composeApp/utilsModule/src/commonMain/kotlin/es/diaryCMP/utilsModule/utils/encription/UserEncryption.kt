package es.diaryCMP.utilsModule.utils.encription


import org.kotlincrypto.SecureRandom
import org.kotlincrypto.hash.sha3.SHA3_256
import org.kotlincrypto.macs.hmac.sha2.HmacSHA256
import kotlin.experimental.xor

object UserEncryption {
    fun generateHash(data: ByteArray): ByteArray {
        val sha256 = SHA3_256()
        return sha256.digest(data)
    }

    fun generateRandom16ByteArray(): ByteArray {
        val random = SecureRandom()
        return random.nextBytesOf(16)
    }

    fun generateUserKey(): ByteArray {
        val keySize = 32
        val secureRandom = SecureRandom()
        return secureRandom.nextBytesOf(keySize)
    }

    fun deriveKey(password: String, salt: ByteArray): ByteArray {
        return pbkdf2HmacSha256(password.encodeToByteArray(), salt, 120_000, 256)
    }

    fun getLocalKey(userKey: ByteArray, salt: ByteArray): ByteArray =
        pbkdf2HmacSha256(userKey, salt, 120_000, 32)

    /*
     * PBKDF2 implementation
     * Generates safely a derived key from a password and a salt.
     * The derived key is derived using the PBKDF2 algorithm.
     */
    private fun pbkdf2HmacSha256(
        password: ByteArray,
        salt: ByteArray,
        iterations: Int,
        dkLen: Int
    ): ByteArray {
        val hmac = HmacSHA256(password)
        val hashLen = hmac.macLength()
        val blockCount = (dkLen + hashLen - 1) / hashLen
        val derivedKey = ByteArray(dkLen)

        for (i in 1..blockCount) {
            val blockIndex = ByteArray(4)
            blockIndex[3] = i.toByte() // LSB is the block index

            var u = hmac.doFinal(salt + blockIndex)
            val block = u.copyOf()

            for (j in 1 until iterations) {
                u = hmac.doFinal(u)
                for (k in block.indices) {
                    block[k] = block[k] xor u[k]
                }
            }

            arrayCopy(block, 0, derivedKey, (i - 1) * hashLen, block.size)
        }

        return derivedKey
    }

    private fun arrayCopy(
        src: ByteArray,
        srcPos: Int,
        dest: ByteArray,
        destPos: Int,
        length: Int
    ) {
        for (i in 0 until length) {
            dest[destPos + i] = src[srcPos + i]
        }
    }
}