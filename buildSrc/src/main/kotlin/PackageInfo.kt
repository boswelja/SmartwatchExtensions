@file:Suppress("SpellCheckingInspection")

object DebugInfo {
    const val idSuffix = ".debug"
}

object PackageInfo {
    const val targetSdk = 33
    const val packageName = "com.boswelja.smartwatchextensions"

    fun getVersionName(): String {
        return try {
            System.getenv("VERSION_NAME")
        } catch (e: Exception) {
            "0.1.0-DEV"
        }
    }

    fun getVersionCode(platformIdentifier: Char): Int {
        val versionParts = getVersionName().split('.')
        // Calculate major part at the start of version code, 1xxxx
        val majorPart = versionParts[0]
        // Calculate minor part in the middle of the version code, x02xx
        val minorPart = versionParts[1].padStart(2, '0')
        // Calculate patch part at the end of the version code, xxx05
        val patchPart = versionParts[2].split('-')[0].padStart(2, '0')
        return (majorPart + minorPart + patchPart + platformIdentifier).toInt()
    }
}
