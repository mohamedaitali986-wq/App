package com.example.util

import android.content.Context
import java.io.File

/**
 * Handles temporary local caching of short videos to ensure recently viewed videos
 * play instantly without re-downloading or buffering lag.
 */
object VideoCacheManager {

    private const val CACHE_DIR_NAME = "shaghaf_video_cache"
    private const val MAX_CACHE_SIZE_BYTES = 100L * 1024L * 1024L // 100 MB max cache

    private fun getCacheDir(context: Context): File {
        val dir = File(context.cacheDir, CACHE_DIR_NAME)
        if (!dir.exists()) {
            dir.mkdirs()
        }
        return dir
    }

    /**
     * Retrieves cached video file if present, or creates a placeholder cache reference.
     */
    fun getCachedVideoFile(context: Context, videoUrlOrName: String): File {
        val fileName = videoUrlOrName.hashCode().toString() + ".mp4"
        val file = File(getCacheDir(context), fileName)
        if (!file.exists()) {
            file.createNewFile()
        }
        return file
    }

    /**
     * Cleans up oldest cache files if total cache exceeds MAX_CACHE_SIZE_BYTES.
     */
    fun trimCache(context: Context) {
        val cacheDir = getCacheDir(context)
        val files = cacheDir.listFiles() ?: return
        var totalSize = files.sumOf { it.length() }

        if (totalSize > MAX_CACHE_SIZE_BYTES) {
            val sortedFiles = files.sortedBy { it.lastModified() }
            for (file in sortedFiles) {
                if (totalSize <= MAX_CACHE_SIZE_BYTES) break
                val length = file.length()
                if (file.delete()) {
                    totalSize -= length
                }
            }
        }
    }
}
