package com.example.wheatherfit.data.local

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(database: SupportSQLiteDatabase) {
        // Add a new table or column, or make other schema changes here
        database.execSQL("ALTER TABLE `users` ADD COLUMN `email` TEXT")
    }
}
