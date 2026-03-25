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

// Room KSP generates the actual implementation.
// iOS stubs are checked into build/generated/ksp/ios*/; KSP overwrites them on macOS.
expect object AppDatabaseConstructor : RoomDatabaseConstructor<AppDatabase> {
    override fun initialize(): AppDatabase
}
