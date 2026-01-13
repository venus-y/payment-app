package im.bigs.pg.external.pg

import org.springframework.stereotype.Component
import java.security.MessageDigest
import java.util.Base64
import javax.crypto.Cipher
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec

@Component
class PgCrypto {

    fun encrypt(
        apiKey: String,
        ivBase64Url: String,
        plainJson: String
    ): String {
        val key = MessageDigest.getInstance("SHA-256")
            .digest(apiKey.toByteArray(Charsets.UTF_8))

        val iv = Base64.getUrlDecoder().decode(ivBase64Url)

        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        val spec = GCMParameterSpec(128, iv)
        cipher.init(Cipher.ENCRYPT_MODE, SecretKeySpec(key, "AES"), spec)

        val cipherText = cipher.doFinal(plainJson.toByteArray(Charsets.UTF_8))
        return Base64.getUrlEncoder().withoutPadding().encodeToString(cipherText)
    }
}