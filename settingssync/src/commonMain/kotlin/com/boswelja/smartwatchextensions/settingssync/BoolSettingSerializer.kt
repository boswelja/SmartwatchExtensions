package com.boswelja.smartwatchextensions.settingssync

import com.boswelja.watchconnection.common.message.MessageSerializer

object BoolSettingSerializer : MessageSerializer<BoolSetting>(
    messagePaths = setOf(UPDATE_BOOL_PREFERENCE)
) {
    override suspend fun deserialize(
        bytes: ByteArray
    ): BoolSetting = BoolSetting.ADAPTER.decode(bytes)

    override suspend fun serialize(data: BoolSetting): ByteArray = data.encode()
}
