package io.github.kroune.cumobile.data.local.db

import androidx.room.ConstructedBy
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.RoomDatabaseConstructor

@Database(
    entities = [FileRenameRuleEntity::class],
    version = 1,
    exportSchema = true,
)
@ConstructedBy(AppDatabaseConstructor::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun fileRenameRuleDao(): FileRenameRuleDao
}

// Room compiler generates the actual implementation on Mac builds.
// Manual actual stubs exist in androidMain/iosMain for Linux CI compatibility.
expect object AppDatabaseConstructor : RoomDatabaseConstructor<AppDatabase> {
    override fun initialize(): AppDatabase
}
