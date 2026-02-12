package com.flit.app.presentation.subscription

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.flit.app.domain.model.SubscriptionFeatures
import com.flit.app.domain.model.SubscriptionTier
import com.flit.app.domain.repository.SubscriptionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * 구독 화면 UI 상태
 */
data class SubscriptionUiState(
    val tier: SubscriptionTier = SubscriptionTier.FREE,
    val features: SubscriptionFeatures = SubscriptionFeatures(),
    val isLoading: Boolean = false,
    val error: String? = null
)

/**
 * 구독 관리 화면 ViewModel
 * 구독 상태 조회 + 업그레이드 처리
 */
@HiltViewModel
class SubscriptionViewModel @Inject constructor(
    private val subscriptionRepository: SubscriptionRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SubscriptionUiState())
    val uiState: StateFlow<SubscriptionUiState> = _uiState.asStateFlow()

    init {
        loadSubscription()
    }

    /**
     * 구독 정보 로드
     */
    private fun loadSubscription() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val (tier, features) = subscriptionRepository.getSubscription()
                _uiState.update {
                    it.copy(tier = tier, features = features, isLoading = false)
                }
            } catch (e: Exception) {
                // 네트워크 실패 시 캐시된 값 사용
                _uiState.update {
                    it.copy(
                        tier = subscriptionRepository.getCachedTier(),
                        features = subscriptionRepository.getCachedFeatures(),
                        isLoading = false
                    )
                }
            }
        }
    }

    /**
     * Premium 업그레이드 (Billing SDK 연동 placeholder)
     */
    fun onUpgrade() {
        _uiState.update {
            it.copy(error = "Google Play Billing 연동이 필요합니다.")
        }
    }
}
