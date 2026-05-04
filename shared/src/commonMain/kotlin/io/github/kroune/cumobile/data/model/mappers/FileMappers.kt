package io.github.kroune.cumobile.data.model.mappers

import io.github.kroune.cumobile.data.local.DownloadedFileInfo
import io.github.kroune.cumobile.data.local.FileRenameRule
import io.github.kroune.cumobile.domain.model.DownloadedFileInfoDomain
import io.github.kroune.cumobile.domain.model.FileRenameRuleDomain

fun DownloadedFileInfo.toDomain(): DownloadedFileInfoDomain =
    DownloadedFileInfoDomain(
        name = name,
        path = path,
        sizeBytes = sizeBytes,
        lastModifiedMillis = lastModifiedMillis,
    )

fun FileRenameRule.toDomain(): FileRenameRuleDomain =
    FileRenameRuleDomain(
        courseId = courseId,
        activityName = activityName,
        extension = extension,
        template = template,
    )

fun FileRenameRuleDomain.toDataLocal(): FileRenameRule =
    FileRenameRule(
        courseId = courseId,
        activityName = activityName,
        extension = extension,
        template = template,
    )
