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
  const val versionName = "2.5.0"

  fun getVersionCode(): Int {
    val versionBuildKey = "version.build_number"
    val versionPropFile = File("version.properties")
    val lastModified = versionPropFile.lastModified()
    val versionProps = Properties()
    if (!versionPropFile.canRead()) {
      if (versionPropFile.createNewFile()) {
        versionProps.load(FileInputStream(versionPropFile))
      } else throw IOException("Unable to open ${versionPropFile.absolutePath}")
    } else {
      versionProps.load(FileInputStream(versionPropFile))
    }

    val versionBuild: Int =
        if (versionProps.containsKey(versionBuildKey) && !isOldData(lastModified)) {
          versionProps[versionBuildKey].toString().toInt()
        } else {
          0
        }
    if (versionBuild < 99) {
      if (shouldIncrementVersion(lastModified)) {
        versionProps[versionBuildKey] = (versionBuild + 1).toString()
        versionProps.store(FileWriter(versionPropFile), null)
      }
    } else {
      throw Exception("Build limit reached")
    }
    val buildNumber = String.format("%02d", versionBuild)

    val dateSection = SimpleDateFormat("yyyyMMdd", Locale.ROOT).format(Date())
    return dateSection.plus(buildNumber).toInt()
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
