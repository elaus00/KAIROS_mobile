package com.flit.app.domain.usecase.capture

import com.flit.app.domain.repository.CaptureRepository
import com.flit.app.domain.repository.ImageRepository
import com.flit.app.domain.repository.NoteRepository
import com.flit.app.domain.repository.ScheduleRepository
import com.flit.app.domain.repository.TagRepository
import com.flit.app.domain.repository.TodoRepository
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 캡처 하드 삭제 UseCase
 * DB 완전 삭제 + 파생 엔티티(Todo/Schedule/Note/Tag 관계) 삭제
 */
@Singleton
class HardDeleteCaptureUseCase @Inject constructor(
    private val captureRepository: CaptureRepository,
    private val todoRepository: TodoRepository,
    private val scheduleRepository: ScheduleRepository,
    private val noteRepository: NoteRepository,
    private val tagRepository: TagRepository,
    private val imageRepository: ImageRepository
) {
    suspend operator fun invoke(captureId: String) {
        val imageUri = captureRepository.getCaptureById(captureId)?.imageUri

        // 파생 엔티티 삭제 (FK CASCADE로 자동 삭제되지만 명시적으로 처리)
        todoRepository.deleteByCaptureId(captureId)
        scheduleRepository.deleteByCaptureId(captureId)
        noteRepository.deleteByCaptureId(captureId)
        tagRepository.deleteTagsByCaptureId(captureId)
        // 캡처 본체 삭제 (FTS 인덱스도 함께 삭제)
        captureRepository.hardDelete(captureId)
        imageUri?.let { imageRepository.deleteImage(it) }
    }
}
