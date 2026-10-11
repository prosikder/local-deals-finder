package com.localdeals.finder.data.local

import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.sqlite.db.SupportSQLiteOpenHelper
import androidx.test.core.app.ApplicationProvider
import android.content.Context
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class MigrationTest {

    @Test
    fun testMigration1To2_addsAvatarUrlColumnPreservingUserData() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val config = SupportSQLiteOpenHelper.Configuration.builder(context)
            .name("test_migration.db")
            .callback(object : SupportSQLiteOpenHelper.Callback(1) {
                override fun onCreate(db: SupportSQLiteDatabase) {
                    db.execSQL("""
                        CREATE TABLE IF NOT EXISTS `users` (
                            `id` TEXT NOT NULL,
                            `name` TEXT NOT NULL,
                            `email` TEXT NOT NULL,
                            `passwordHash` TEXT NOT NULL,
                            `passwordSalt` TEXT NOT NULL,
                            `phone` TEXT NOT NULL,
                            `role` TEXT NOT NULL,
                            `searchRadiusKm` INTEGER NOT NULL,
                            `language` TEXT NOT NULL,
                            `isBlocked` INTEGER NOT NULL,
                            PRIMARY KEY(`id`)
                        )
                    """.trimIndent())
                }

                override fun onUpgrade(db: SupportSQLiteDatabase, oldVersion: Int, newVersion: Int) {}
            })
            .build()

        val helper = FrameworkSQLiteOpenHelperFactory().create(config)
        val db = helper.writableDatabase

        // Insert initial v1 user
        db.execSQL("""
            INSERT INTO users (id, name, email, passwordHash, passwordSalt, phone, role, searchRadiusKm, language, isBlocked)
            VALUES ('u1', 'Alice', 'alice@test.com', 'h1', 's1', '017', 'USER', 5, 'English', 0)
        """)

        // Execute stub migration 1 -> 2
        DatabaseMigrations.MIGRATION_1_2.migrate(db)

        // Query the table and check that avatarUrl column is accessible and defaults to empty string
        val cursor = db.query("SELECT id, name, avatarUrl FROM users WHERE id = 'u1'")
        assertTrue(cursor.moveToFirst())
        assertEquals("u1", cursor.getString(0))
        assertEquals("Alice", cursor.getString(1))
        assertEquals("", cursor.getString(2))
        cursor.close()

        // Verify new column accepts values
        db.execSQL("UPDATE users SET avatarUrl = 'https://example.com/avatar.png' WHERE id = 'u1'")
        val updatedCursor = db.query("SELECT avatarUrl FROM users WHERE id = 'u1'")
        assertTrue(updatedCursor.moveToFirst())
        assertEquals("https://example.com/avatar.png", updatedCursor.getString(0))
        updatedCursor.close()

        db.close()
    }
}
