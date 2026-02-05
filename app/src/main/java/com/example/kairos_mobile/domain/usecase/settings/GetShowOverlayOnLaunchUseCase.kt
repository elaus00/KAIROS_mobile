package com.example.kairos_mobile.domain.usecase.settings

import com.example.kairos_mobile.domain.repository.PreferencesRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 앱 시작 시 QuickCapture 오버레이 표시 설정 조회 Use Case
 */
@Singleton
class GetShowOverlayOnLaunchUseCase @Inject constructor(
    private val repository: PreferencesRepository
) {
    /**
     * 오버레이 표시 설정 조회
     *
     * @return 활성화 여부 Flow (기본값: true)
     */
    operator fun invoke(): Flow<Boolean> {
        return repository.getShowOverlayOnLaunch()
    }
}
