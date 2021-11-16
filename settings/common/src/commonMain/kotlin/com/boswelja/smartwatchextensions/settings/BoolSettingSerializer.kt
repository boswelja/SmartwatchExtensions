package com.boswelja.smartwatchextensions.settings

import com.boswelja.watchconnection.serialization.MessageSerializer

/**
 * A [MessageSerializer] for handling [BoolSetting].
 */
object BoolSettingSerializer : MessageSerializer<BoolSetting> {

    override val messagePaths: Set<String> = setOf(UPDATE_BOOL_PREFERENCE)

    override suspend fun deserialize(
        bytes: ByteArray?
    ): BoolSetting = BoolSetting.ADAPTER.decode(bytes!!)

    override suspend fun serialize(data: BoolSetting): ByteArray = data.encode()
}
