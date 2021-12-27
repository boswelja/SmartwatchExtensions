package com.boswelja.smartwatchextensions.messages

import app.cash.turbine.test
import com.boswelja.smartwatchextensions.messages.database.MessageDatabase
import com.boswelja.smartwatchextensions.messages.database.messagesDbAdapter
import com.squareup.sqldelight.db.SqlDriver
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.time.ExperimentalTime

class MessagesDbRepositoryTest {

    private lateinit var repository: MessagesRepository

    @BeforeTest
    fun setUp() {
        val driver = createTestSqlDriver().apply {
            MessageDatabase.Schema.create(this)
        }
        val database = MessageDatabase(driver, messagesDbAdapter)
        repository = MessagesDbRepository(database, Dispatchers.Default)
    }

    @Test
    fun insertInsertsMessage() = runSuspendingTest {
        val testMessage = Message(
            icon = Message.Icon.ERROR,
            title = "SomeUniqueTitle",
            text = "Some text",
            action = Message.Action.NONE,
            timestamp = 0
        )

        // Insert message
        repository.insert(testMessage, null)

        // Check message exists
        assertTrue {
            repository.getAllWhere(false).first().any { it.title == testMessage.title }
        }
    }

    @Test
    fun archiveArchivesMessage() = runSuspendingTest {
        val testMessage = Message(
            icon = Message.Icon.ERROR,
            title = "SomeUniqueTitle",
            text = "Some text",
            action = Message.Action.NONE,
            timestamp = 0
        )

        // Insert message
        repository.insert(testMessage, null)

        // Get message ID
        val id = repository.getAllWhere(false)
            .first()
            .first { it.title == testMessage.title }
            .id

        // Set archived and check result
        repository.archive(id)
        assertTrue {
            repository.getAllWhere(true).first().any { it.title == testMessage.title }
        }
    }

    @Test
    fun deleteArchivedDeletesArchived() = runSuspendingTest {
        // Archive a message
        archiveArchivesMessage()

        // Delete archived and check value
        repository.deleteArchived()
        assertTrue { repository.getAllWhere(true).first().isEmpty() }
    }

    @Test
    fun deleteForSourceDeletesForSource() = runSuspendingTest {
        val sourceUid = "uid"
        val testMessage = Message(
            icon = Message.Icon.ERROR,
            title = "SomeUniqueTitle",
            text = "Some text",
            action = Message.Action.NONE,
            timestamp = 0
        )

        // Insert message
        repository.insert(testMessage, sourceUid)

        // Delete for source and check result
        repository.deleteForSource(sourceUid)
        assertTrue {
            repository.getAllWhere(false).first().none { it.sourceUid == sourceUid }
        }
    }

    @OptIn(ExperimentalTime::class)
    @Test
    fun getAllWhereFlowsUpdates() = runSuspendingTest {
        // Insert a message
        repository.insert(
            Message(
                icon = Message.Icon.ERROR,
                title = "SomeUniqueTitle",
                text = "Some text",
                action = Message.Action.NONE,
                timestamp = 0
            ),
            null
        )

        repository.getAllWhere(false).test {
            // Check initial value
            assertEquals(1, awaitItem().count())

            // Add 1 and check again
            repository.insert(
                Message(
                    icon = Message.Icon.ERROR,
                    title = "SomeUniqueTitle",
                    text = "Some text",
                    action = Message.Action.NONE,
                    timestamp = 0
                ),
                null
            )
            val items = awaitItem()
            assertEquals(2, items.count())

            // Remove 1 and check again'
            val id = items.first().id
            repository.archive(id)
            assertEquals(1, awaitItem().count())
        }
    }
}

expect fun createTestSqlDriver(): SqlDriver
