@file:Suppress("SpellCheckingInspection")

import java.io.File
import java.io.FileInputStream
import java.io.FileWriter
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.Properties
import java.util.concurrent.TimeUnit

object DebugInfo {
    const val idSuffix = ".debug"
}

object PackageInfo {
    const val packageName = "com.boswelja.devicemanager"
    const val versionName = "2.5.4"

    fun getVersionCode(): Int {
        val dateSection = SimpleDateFormat("yyyyMMddHH", Locale.ROOT).format(Date())
        return dateSection.toInt()
    }

    private fun shouldIncrementVersion(lastEditedTimestamp: Long): Boolean {
        val lastBuildDate = Calendar.getInstance()
        lastBuildDate.timeInMillis = lastEditedTimestamp

        val currentDate = Calendar.getInstance()
        currentDate.timeInMillis = System.currentTimeMillis()

        val minimumDiff = TimeUnit.SECONDS.toMillis(45)
        return (currentDate.timeInMillis - lastBuildDate.timeInMillis) >= minimumDiff
    }

    private fun isOldData(timestamp: Long): Boolean {
        val lastBuildDate = Calendar.getInstance()
        lastBuildDate.timeInMillis = timestamp
        lastBuildDate.set(Calendar.HOUR_OF_DAY, 0)
        lastBuildDate.set(Calendar.MINUTE, 0)
        lastBuildDate.set(Calendar.SECOND, 0)
        lastBuildDate.set(Calendar.MILLISECOND, 0)

        val currentDate = Calendar.getInstance()
        currentDate.timeInMillis = System.currentTimeMillis()
        currentDate.set(Calendar.HOUR_OF_DAY, 0)
        currentDate.set(Calendar.MINUTE, 0)
        currentDate.set(Calendar.SECOND, 0)
        currentDate.set(Calendar.MILLISECOND, 0)
        return lastBuildDate < currentDate
    }
}
