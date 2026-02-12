package com.flit.app.domain.usecase.note

import com.flit.app.domain.model.ApiException
import com.flit.app.domain.model.Folder
import com.flit.app.domain.model.NoteAiInput
import com.flit.app.domain.model.ProposedStructure
import com.flit.app.domain.model.SubscriptionTier
import com.flit.app.domain.repository.NoteAiRepository
import com.flit.app.domain.repository.SubscriptionRepository
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
