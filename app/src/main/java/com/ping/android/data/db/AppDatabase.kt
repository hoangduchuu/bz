package com.ping.android.data.db

import com.ping.android.data.entity.MessageEntity
import com.raizlabs.android.dbflow.annotation.Database
import com.raizlabs.android.dbflow.annotation.Migration
import com.raizlabs.android.dbflow.sql.SQLiteType
import com.raizlabs.android.dbflow.sql.migration.AlterTableMigration

@Database(name = AppDatabase.NAME, version = AppDatabase.VERSION)
object AppDatabase {
    const val NAME = "BZZZ"
    const val VERSION = 4

    @Migration(version = VERSION, database = AppDatabase::class)
    class Migration2(table: Class<MessageEntity>): AlterTableMigration<MessageEntity>(table) {
        override fun onPreMigrate() {
            addColumn(SQLiteType.INTEGER, "messageStatusCode")
        }
    }
}