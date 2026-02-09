package com.example.kairos_mobile.domain.usecase.note

import com.example.kairos_mobile.domain.model.ApiException
import com.example.kairos_mobile.domain.model.NoteGroup
import com.example.kairos_mobile.domain.model.SubscriptionTier
import com.example.kairos_mobile.domain.repository.NoteAiRepository
import com.example.kairos_mobile.domain.repository.SubscriptionRepository
import javax.inject.Inject

/** AI 노트 자동 그룹화 (프리미엄 전용) */
class AutoGroupNotesUseCase @Inject constructor(
    private val noteAiRepository: NoteAiRepository,
    private val subscriptionRepository: SubscriptionRepository
) {
    suspend operator fun invoke(noteIds: List<String>): List<NoteGroup> {
        if (subscriptionRepository.getCachedTier() != SubscriptionTier.PREMIUM) {
            throw ApiException.SubscriptionRequired()
        }
        return noteAiRepository.groupNotes(noteIds)
    }
}
