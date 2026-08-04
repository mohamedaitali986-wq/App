package com.example.util

import java.security.MessageDigest
import java.security.SecureRandom
import java.util.Base64

/**
 * Security utility for hashing passwords using SHA-256 with unique per-user salt.
 * Ensures no plaintext passwords are saved in the database or logged anywhere.
 */
object SecurityUtils {

    /**
     * Generates a random 16-byte salt encoded in Base64.
     */
    fun generateSalt(): String {
        val random = SecureRandom()
        val saltBytes = ByteArray(16)
        random.nextBytes(saltBytes)
        return Base64.getEncoder().encodeToString(saltBytes)
    }

    /**
     * Hashes a plaintext password combined with a salt using SHA-256.
     */
    fun hashPassword(password: String, salt: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val combinedBytes = (salt + password).toByteArray(Charsets.UTF_8)
        val hashBytes = digest.digest(combinedBytes)
        return Base64.getEncoder().encodeToString(hashBytes)
    }

    /**
     * Verifies if a provided plaintext password matches the stored hash for a given salt.
     */
    fun verifyPassword(password: String, salt: String, storedHash: String): Boolean {
        val newHash = hashPassword(password, salt)
        return newHash == storedHash
    }
}
