package es.diaryCMP.utilsModule.utils.encription

import com.ionspin.kotlin.crypto.LibsodiumInitializer
import com.ionspin.kotlin.crypto.secretbox.SecretBox
import com.ionspin.kotlin.crypto.util.LibsodiumRandom
import com.ionspin.kotlin.crypto.util.decodeFromUByteArray
import com.ionspin.kotlin.crypto.util.encodeToUByteArray

/**
 * Encrypts a text using a key
 *
 * @param plainText The text to encrypt
 * @param key The key to encrypt with
 * @return The nonce and the encrypted text, concatenated.
 */
@OptIn(ExperimentalUnsignedTypes::class)
suspend fun encrypt(plainText: String, key: ByteArray): ByteArray {
    safeInitializeLibsodium()

    val nonce = LibsodiumRandom.buf(24)

    val encrypted = SecretBox.easy(
        message = plainText.encodeToUByteArray(),
        nonce = nonce,
        key = key.toUByteArray()
    )
    return (nonce + encrypted).toByteArray()
}

/**
 * Decrypts a text using a key
 *
 * @param cipherText The text to decrypt, the nonce and the encrypted text concatenated
 * @param key The key to decrypt with
 * @return The decrypted text
 */
@OptIn(ExperimentalUnsignedTypes::class)
suspend fun decrypt(cipherText: ByteArray, key: ByteArray): String {
    safeInitializeLibsodium()

    val nonce = cipherText.copyOfRange(0, 24)
    val encrypted = cipherText.copyOfRange(24, cipherText.size)
    return SecretBox.openEasy(
        encrypted.toUByteArray(),
        nonce.toUByteArray(),
        key.toUByteArray()
    ).decodeFromUByteArray()
}

private suspend fun safeInitializeLibsodium() {
    if (!LibsodiumInitializer.isInitialized())
        LibsodiumInitializer.initialize()
}
