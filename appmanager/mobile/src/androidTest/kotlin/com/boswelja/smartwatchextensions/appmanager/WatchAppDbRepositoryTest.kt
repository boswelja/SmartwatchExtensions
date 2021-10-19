package com.boswelja.smartwatchextensions.appmanager

import com.squareup.sqldelight.db.SqlDriver
import com.squareup.sqldelight.sqlite.driver.JdbcSqliteDriver

actual fun createTestSqlDriver(): SqlDriver {
    return JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY)
}
