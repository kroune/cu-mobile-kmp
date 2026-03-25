package io.github.kroune.cumobile.data.local.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface FileRenameRuleDao {
    @Query("SELECT * FROM file_rename_rules")
    fun getAllRules(): Flow<List<FileRenameRuleEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRule(rule: FileRenameRuleEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRules(rules: List<FileRenameRuleEntity>)

    @Delete
    suspend fun deleteRule(rule: FileRenameRuleEntity)

    @Query("DELETE FROM file_rename_rules")
    suspend fun deleteAllRules()

    @Query(
        "SELECT * FROM file_rename_rules " +
            "WHERE courseId = :courseId " +
            "AND activityName = :activityName COLLATE NOCASE " +
            "AND extension = :extension COLLATE NOCASE " +
            "LIMIT 1",
    )
    suspend fun findMatchingRule(
        courseId: String,
        activityName: String,
        extension: String,
    ): FileRenameRuleEntity?

    @Transaction
    suspend fun replaceAllRules(rules: List<FileRenameRuleEntity>) {
        deleteAllRules()
        insertRules(rules)
    }
}
