package io.github.kroune.cumobile.presentation.common.model.mappers

import io.github.kroune.cumobile.domain.model.DownloadedFileInfoDomain
import io.github.kroune.cumobile.domain.model.FileRenameRuleDomain
import io.github.kroune.cumobile.presentation.common.formatEpochDate
import io.github.kroune.cumobile.presentation.common.formatSizeBytes
import io.github.kroune.cumobile.presentation.common.model.DownloadedFileInfoUi
import io.github.kroune.cumobile.presentation.common.model.FileRenameRuleUi

fun DownloadedFileInfoDomain.toUi(): DownloadedFileInfoUi =
    DownloadedFileInfoUi(
        name = name,
        path = path,
        sizeBytes = sizeBytes,
        extension = extension,
        sizeLabel = formatSizeBytes(sizeBytes),
        dateLabel = formatEpochDate(lastModifiedMillis),
    )

fun FileRenameRuleDomain.toUi(): FileRenameRuleUi =
    FileRenameRuleUi(
        courseId = courseId,
        activityName = activityName,
        extension = extension,
        template = template,
    )

fun FileRenameRuleUi.toDomain(): FileRenameRuleDomain =
    FileRenameRuleDomain(
        courseId = courseId,
        activityName = activityName,
        extension = extension,
        template = template,
    )
