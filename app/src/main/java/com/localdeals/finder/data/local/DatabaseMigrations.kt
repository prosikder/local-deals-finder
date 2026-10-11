package com.localdeals.finder.data.local

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * Migration Strategy for Local Deals Finder:
 *
 * 1. Schema Versioning: AppDatabase version increments with every schema change (1 -> 2, etc.).
 * 2. Incremental Migrations: Every version upgrade defines an explicit [Migration] object
 *    containing SQLite DDL statements (ALTER TABLE, CREATE TABLE, CREATE INDEX).
 * 3. Production Data Safety: Destructive migrations are avoided in production so user orders,
 *    wishlists, and seller configurations remain intact.
 * 4. Verification: Schema JSON files are exported to app/schemas and validated via migration tests.
 *
 * [MIGRATION_1_2] is a documented, tested stub migration demonstrating adding an optional avatarUrl
 * column to the users table without data loss.
 */
object DatabaseMigrations {
    val MIGRATION_1_2 = object : Migration(1, 2) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("ALTER TABLE users ADD COLUMN avatarUrl TEXT NOT NULL DEFAULT ''")
        }
    }

    val ALL_MIGRATIONS = arrayOf(
        MIGRATION_1_2
    )
}
