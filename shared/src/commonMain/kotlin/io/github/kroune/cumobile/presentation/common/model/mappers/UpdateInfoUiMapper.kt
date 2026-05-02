package io.github.kroune.cumobile.presentation.common.model.mappers

import io.github.kroune.cumobile.domain.model.UpdateInfoDomain
import io.github.kroune.cumobile.presentation.common.model.UpdateInfoUi

fun UpdateInfoDomain.toUi(): UpdateInfoUi =
    UpdateInfoUi(
        latestVersion = latestVersion,
        releasePageUrl = releasePageUrl,
        apkDownloadUrl = apkDownloadUrl,
        releaseName = releaseName,
    )
