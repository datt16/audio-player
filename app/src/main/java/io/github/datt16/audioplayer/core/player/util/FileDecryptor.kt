package io.github.datt16.audioplayer.core.player.util

import java.io.File
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

fun decryptFileEncryptedByAesCBC(encryptedFile: File, key: String, iv: String): ByteArray {
  val keyBytes = key.decodeHex()
  val ivBytes = iv.decodeHex()

  val cipher = Cipher.getInstance("AES/CBC/NoPadding")
  val keySpec = SecretKeySpec(keyBytes, "AES")
  val ivSpec = IvParameterSpec(ivBytes)
  cipher.init(Cipher.DECRYPT_MODE, keySpec, ivSpec)

  return encryptedFile.readBytes().let { encrypted ->
    cipher.doFinal(encrypted)
  }
}

private fun String.decodeHex(): ByteArray {
  return chunked(2).map { it.toInt(16).toByte() }.toByteArray()
}
