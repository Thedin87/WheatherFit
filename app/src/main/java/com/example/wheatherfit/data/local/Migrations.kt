package com.example.wheatherfit.data.local

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(database: SupportSQLiteDatabase) {
        // Add a new table or column, or make other schema changes here
        database.execSQL("ALTER TABLE `user` ADD COLUMN `email` TEXT")
    }
}

val MIGRATION_2_3 = object : Migration(2, 3) {

    override fun migrate(database: SupportSQLiteDatabase) {
        // Add a new table or column, or make other schema changes here
        database.execSQL("ALTER TABLE `users` RENAME TO `user`")
        database.execSQL("ALTER TABLE user ADD COLUMN firstname TEXT DEFAULT 'undefined'")
        database.execSQL("ALTER TABLE user ADD COLUMN lastname TEXT DEFAULT 'undefined'")
        database.execSQL("ALTER TABLE user ADD COLUMN city TEXT DEFAULT 'undefined'")
        database.execSQL("ALTER TABLE user ADD COLUMN country TEXT DEFAULT 'undefined'")
    }

}
