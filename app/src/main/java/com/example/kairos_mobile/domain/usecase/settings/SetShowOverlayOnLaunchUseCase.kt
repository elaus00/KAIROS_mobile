package com.example.kairos_mobile.domain.usecase.settings

import com.example.kairos_mobile.domain.repository.PreferencesRepository
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 앱 시작 시 QuickCapture 오버레이 표시 설정 변경 Use Case
 */
@Singleton
class SetShowOverlayOnLaunchUseCase @Inject constructor(
    private val repository: PreferencesRepository
) {
    /**
     * 오버레이 표시 설정 변경
     *
     * @param enabled 활성화 여부
     */
    suspend operator fun invoke(enabled: Boolean) {
        repository.setShowOverlayOnLaunch(enabled)
    }
}
