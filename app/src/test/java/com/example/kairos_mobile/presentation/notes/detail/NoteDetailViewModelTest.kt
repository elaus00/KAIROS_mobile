package com.example.kairos_mobile.presentation.notes.detail

import androidx.lifecycle.SavedStateHandle
import com.example.kairos_mobile.domain.model.ClassifiedType
import com.example.kairos_mobile.domain.model.NoteDetail
import com.example.kairos_mobile.domain.model.NoteSubType
import com.example.kairos_mobile.domain.usecase.analytics.TrackEventUseCase
import com.example.kairos_mobile.domain.usecase.folder.GetAllFoldersUseCase
import com.example.kairos_mobile.domain.usecase.note.GetNoteDetailUseCase
import com.example.kairos_mobile.domain.usecase.note.UpdateNoteUseCase
import com.example.kairos_mobile.util.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.unmockkAll
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

/**
 * NoteDetailViewModel 단위 테스트
 * - 노트 상세 로드
 * - 편집 상태 추적 (제목/본문/폴더)
 * - 저장 위임
 * - capture_revisited 분석 이벤트
 * - 에러 처리
 */
@OptIn(ExperimentalCoroutinesApi::class)
class NoteDetailViewModelTest {

    @get:Rule
    val dispatcherRule = MainDispatcherRule()

    private lateinit var getNoteDetailUseCase: GetNoteDetailUseCase
    private lateinit var updateNoteUseCase: UpdateNoteUseCase
    private lateinit var getAllFoldersUseCase: GetAllFoldersUseCase
    private lateinit var trackEventUseCase: TrackEventUseCase

    @Before
    fun setup() {
        getNoteDetailUseCase = mockk()
        updateNoteUseCase = mockk()
        getAllFoldersUseCase = mockk()
        trackEventUseCase = mockk(relaxed = true)
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    /**
     * 테스트용 NoteDetail 생성
     */
    private fun createNoteDetail(
        noteId: String = "note-1",
        captureId: String = "cap-1",
        aiTitle: String? = "테스트 노트",
        originalText: String = "원본 텍스트",
        body: String? = null,
        folderId: String? = "system-inbox",
        createdAt: Long = 1000L
    ) = NoteDetail(
        noteId = noteId,
        captureId = captureId,
        aiTitle = aiTitle,
        originalText = originalText,
        body = body,
        classifiedType = ClassifiedType.NOTES,
        noteSubType = NoteSubType.INBOX,
        folderId = folderId,
        imageUri = null,
        createdAt = createdAt,
        updatedAt = createdAt
    )

    /**
     * ViewModel 생성 헬퍼 — init에서 loadNoteDetail/loadFolders 호출
     */
    private fun createViewModel(noteId: String = "note-1"): NoteDetailViewModel {
        val savedStateHandle = SavedStateHandle(mapOf("noteId" to noteId))
        return NoteDetailViewModel(
            savedStateHandle,
            getNoteDetailUseCase,
            updateNoteUseCase,
            getAllFoldersUseCase,
            trackEventUseCase
        )
    }

    // ── 1. 노트 미존재 시 에러 상태 ──

    @Test
    fun `note_not_found_sets_error`() = runTest {
        // given: 노트 없음
        every { getNoteDetailUseCase("note-1") } returns flowOf(null)
        every { getAllFoldersUseCase() } returns flowOf(emptyList())

        // when
        val viewModel = createViewModel()
        advanceUntilIdle()

        // then
        assertFalse(viewModel.uiState.value.isLoading)
        assertEquals("노트를 찾을 수 없습니다", viewModel.uiState.value.error)
    }

    // ── 2. 노트 로드 시 상태 반영 ──

    @Test
    fun `load_note_detail_populates_state`() = runTest {
        // given
        val noteDetail = createNoteDetail(aiTitle = "테스트 제목", body = "노트 본문")
        every { getNoteDetailUseCase("note-1") } returns flowOf(noteDetail)
        every { getAllFoldersUseCase() } returns flowOf(emptyList())

        // when
        val viewModel = createViewModel()
        advanceUntilIdle()

        // then
        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertEquals(noteDetail, state.noteDetail)
        assertEquals("테스트 제목", state.editedTitle)
        assertEquals("노트 본문", state.editedBody)
        assertEquals("system-inbox", state.selectedFolderId)
    }

    // ── 3. 제목 변경 → hasChanges ──

    @Test
    fun `edit_title_sets_hasChanges_true`() = runTest {
        // given
        val noteDetail = createNoteDetail(aiTitle = "원래 제목")
        every { getNoteDetailUseCase("note-1") } returns flowOf(noteDetail)
        every { getAllFoldersUseCase() } returns flowOf(emptyList())

        val viewModel = createViewModel()
        advanceUntilIdle()
        assertFalse(viewModel.uiState.value.hasChanges)

        // when
        viewModel.onTitleChanged("수정된 제목")

        // then
        assertTrue(viewModel.uiState.value.hasChanges)
        assertEquals("수정된 제목", viewModel.uiState.value.editedTitle)
    }

    // ── 4. 본문 변경 → hasChanges ──

    @Test
    fun `edit_body_sets_hasChanges_true`() = runTest {
        // given
        val noteDetail = createNoteDetail(body = "원래 본문")
        every { getNoteDetailUseCase("note-1") } returns flowOf(noteDetail)
        every { getAllFoldersUseCase() } returns flowOf(emptyList())

        val viewModel = createViewModel()
        advanceUntilIdle()

        // when
        viewModel.onBodyChanged("수정된 본문")

        // then
        assertTrue(viewModel.uiState.value.hasChanges)
        assertEquals("수정된 본문", viewModel.uiState.value.editedBody)
    }

    // ── 5. 폴더 변경 → hasChanges ──

    @Test
    fun `folder_change_sets_hasChanges_true`() = runTest {
        // given
        val noteDetail = createNoteDetail(folderId = "system-inbox")
        every { getNoteDetailUseCase("note-1") } returns flowOf(noteDetail)
        every { getAllFoldersUseCase() } returns flowOf(emptyList())

        val viewModel = createViewModel()
        advanceUntilIdle()

        // when
        viewModel.onFolderChanged("folder-ideas")

        // then
        assertTrue(viewModel.uiState.value.hasChanges)
        assertEquals("folder-ideas", viewModel.uiState.value.selectedFolderId)
    }

    // ── 6. 저장 → UseCase 위임 ──

    @Test
    fun `save_delegates_title_body_folder_to_usecase`() = runTest {
        // given
        val noteDetail = createNoteDetail(aiTitle = "원래", body = "원본", folderId = "f1")
        every { getNoteDetailUseCase("note-1") } returns flowOf(noteDetail)
        every { getAllFoldersUseCase() } returns flowOf(emptyList())
        coEvery { updateNoteUseCase.updateTitle("cap-1", any()) } just runs
        coEvery { updateNoteUseCase.updateBody("note-1", any()) } just runs
        coEvery { updateNoteUseCase.moveToFolder("note-1", any()) } just runs

        val viewModel = createViewModel()
        advanceUntilIdle()

        // when: 제목, 본문, 폴더 모두 변경 후 저장
        viewModel.onTitleChanged("새 제목")
        viewModel.onBodyChanged("새 본문")
        viewModel.onFolderChanged("f2")
        viewModel.onSave()
        advanceUntilIdle()

        // then
        coVerify { updateNoteUseCase.updateTitle("cap-1", "새 제목") }
        coVerify { updateNoteUseCase.updateBody("note-1", "새 본문") }
        coVerify { updateNoteUseCase.moveToFolder("note-1", "f2") }
        assertFalse(viewModel.uiState.value.hasChanges)
        assertFalse(viewModel.uiState.value.isSaving)
    }

    // ── 7. capture_revisited 이벤트 ──

    @Test
    fun `capture_revisited_event_tracked_on_first_load`() = runTest {
        // given
        val noteDetail = createNoteDetail(createdAt = 1000L)
        every { getNoteDetailUseCase("note-1") } returns flowOf(noteDetail)
        every { getAllFoldersUseCase() } returns flowOf(emptyList())

        // when
        createViewModel()
        advanceUntilIdle()

        // then: capture_revisited 이벤트 1회 발행
        coVerify(exactly = 1) {
            trackEventUseCase(
                "capture_revisited",
                match { it.contains("time_since_creation_ms") && it.contains("access_method") }
            )
        }
    }

    // ── 8. 원본 텍스트 토글 ──

    @Test
    fun `toggle_original_text_flips_flag`() = runTest {
        // given
        val noteDetail = createNoteDetail()
        every { getNoteDetailUseCase("note-1") } returns flowOf(noteDetail)
        every { getAllFoldersUseCase() } returns flowOf(emptyList())

        val viewModel = createViewModel()
        advanceUntilIdle()
        assertFalse(viewModel.uiState.value.showOriginalText)

        // when: 토글 on
        viewModel.onToggleOriginalText()
        assertTrue(viewModel.uiState.value.showOriginalText)

        // when: 토글 off
        viewModel.onToggleOriginalText()
        assertFalse(viewModel.uiState.value.showOriginalText)
    }

    // ── 9. 에러 닫기 ──

    @Test
    fun `error_dismissed_clears_error`() = runTest {
        // given: 저장 실패로 에러 발생
        val noteDetail = createNoteDetail(aiTitle = "원래")
        every { getNoteDetailUseCase("note-1") } returns flowOf(noteDetail)
        every { getAllFoldersUseCase() } returns flowOf(emptyList())
        coEvery { updateNoteUseCase.updateTitle(any(), any()) } throws RuntimeException("저장 실패")

        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onTitleChanged("에러 유발")
        viewModel.onSave()
        advanceUntilIdle()
        assertNotNull(viewModel.uiState.value.error)

        // when
        viewModel.onErrorDismissed()

        // then
        assertNull(viewModel.uiState.value.error)
    }
}
