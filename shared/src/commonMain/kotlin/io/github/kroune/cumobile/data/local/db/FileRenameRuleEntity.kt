package io.github.kroune.cumobile.data.local.db

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import io.github.kroune.cumobile.data.local.FileRenameRule

@Entity(
    tableName = "file_rename_rules",
    indices = [Index(value = ["courseId", "activityName", "extension"], unique = true)],
)
data class FileRenameRuleEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val courseId: String,
    val activityName: String,
    val extension: String,
    val template: String,
)

fun FileRenameRuleEntity.toDomain(): FileRenameRule =
    FileRenameRule(
        courseId = courseId,
        activityName = activityName,
        extension = extension,
        template = template,
    )

fun FileRenameRule.toEntity(): FileRenameRuleEntity =
    FileRenameRuleEntity(
        courseId = courseId,
        activityName = activityName,
        extension = extension,
        template = template,
    )
