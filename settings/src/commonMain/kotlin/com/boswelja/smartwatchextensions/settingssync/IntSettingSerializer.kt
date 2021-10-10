package com.boswelja.smartwatchextensions.settingssync

import com.boswelja.smartwatchextensions.setingssync.IntSetting
import com.boswelja.watchconnection.common.message.MessageSerializer

object IntSettingSerializer : MessageSerializer<IntSetting>(
    messagePaths = setOf(UPDATE_INT_PREFERENCE)
) {
    override suspend fun deserialize(
        bytes: ByteArray
    ): IntSetting = IntSetting.ADAPTER.decode(bytes)

    override suspend fun serialize(data: IntSetting): ByteArray = data.encode()
}
