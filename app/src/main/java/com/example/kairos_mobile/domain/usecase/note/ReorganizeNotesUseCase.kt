package com.example.kairos_mobile.domain.usecase.note

import com.example.kairos_mobile.domain.model.ApiException
import com.example.kairos_mobile.domain.model.SubscriptionTier
import com.example.kairos_mobile.domain.repository.NoteAiRepository
import com.example.kairos_mobile.domain.repository.SubscriptionRepository
import javax.inject.Inject

/** 노트 재정리 (프리미엄 전용) */
class ReorganizeNotesUseCase @Inject constructor(
    private val noteAiRepository: NoteAiRepository,
    private val subscriptionRepository: SubscriptionRepository
) {
    /** @return (이동 목록, 새 폴더 목록) */
    suspend operator fun invoke(folderId: String? = null): Pair<List<Pair<String, String>>, List<Pair<String, String>>> {
        if (subscriptionRepository.getCachedTier() != SubscriptionTier.PREMIUM) {
            throw ApiException.SubscriptionRequired()
        }
        return noteAiRepository.reorganize(folderId)
    }
}
