package io.github.kroune.cumobile.data.model.mappers

import io.github.kroune.cumobile.data.model.MaterialAttachmentApi
import io.github.kroune.cumobile.data.model.StartTaskResponseApi
import io.github.kroune.cumobile.data.model.UploadLinkDataApi
import io.github.kroune.cumobile.domain.model.MaterialAttachmentDomain
import io.github.kroune.cumobile.domain.model.StartTaskResponseDomain
import io.github.kroune.cumobile.domain.model.UploadLinkDataDomain

fun MaterialAttachmentApi.toDomain(): MaterialAttachmentDomain =
    MaterialAttachmentDomain(
        name = name,
        filename = filename,
        mediaType = mediaType,
        length = length,
        version = version,
    )

fun MaterialAttachmentDomain.toApi(): MaterialAttachmentApi =
    MaterialAttachmentApi(
        name = name,
        filename = filename,
        mediaType = mediaType,
        length = length,
        version = version,
    )

fun UploadLinkDataApi.toDomain(): UploadLinkDataDomain =
    UploadLinkDataDomain(
        shortName = shortName,
        filename = filename,
        objectKey = objectKey,
        version = version,
        url = url,
    )

fun StartTaskResponseApi.toDomain(): StartTaskResponseDomain =
    StartTaskResponseDomain(
        quizSessionId,
    )
