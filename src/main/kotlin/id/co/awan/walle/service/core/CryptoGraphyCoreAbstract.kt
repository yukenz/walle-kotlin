package id.co.awan.walle.service.core

import org.bouncycastle.jcajce.provider.digest.Keccak
import org.bouncycastle.util.encoders.Hex


abstract class CryptoGraphyCoreAbstract {
    fun keccak256(input: String) = keccak256(input.toByteArray())
    fun keccak256(input: ByteArray): String {

        // Create Keccak-256 digest
        val keccak256 = Keccak.Digest256()
        val hashBytes = keccak256.digest(input)
        return Hex.toHexString(hashBytes)
    }

}