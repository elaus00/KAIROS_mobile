package com.example.kairos_mobile.domain.usecase.note

import com.example.kairos_mobile.domain.model.ApiException
import com.example.kairos_mobile.domain.model.Folder
import com.example.kairos_mobile.domain.model.NoteAiInput
import com.example.kairos_mobile.domain.model.ProposedStructure
import com.example.kairos_mobile.domain.model.SubscriptionTier
import com.example.kairos_mobile.domain.repository.NoteAiRepository
import com.example.kairos_mobile.domain.repository.SubscriptionRepository
import javax.inject.Inject

/** 노트 재정리 (프리미엄 전용) */
class ReorganizeNotesUseCase @Inject constructor(
    private val noteAiRepository: NoteAiRepository,
    private val subscriptionRepository: SubscriptionRepository
) {
    suspend operator fun invoke(notes: List<NoteAiInput>, folders: List<Folder>): List<ProposedStructure> {
        if (subscriptionRepository.getCachedTier() != SubscriptionTier.PREMIUM) {
            throw ApiException.SubscriptionRequired()
        }
        return noteAiRepository.reorganize(notes, folders)
    }
}
