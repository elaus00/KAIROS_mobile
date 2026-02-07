package com.example.kairos_mobile.presentation.notes

import com.example.kairos_mobile.domain.model.Folder
import com.example.kairos_mobile.domain.model.FolderType
import com.example.kairos_mobile.domain.model.NoteWithCapturePreview
import com.example.kairos_mobile.domain.repository.NoteRepository
import com.example.kairos_mobile.domain.usecase.folder.CreateFolderUseCase
import com.example.kairos_mobile.domain.usecase.folder.DeleteFolderUseCase
import com.example.kairos_mobile.domain.usecase.folder.GetAllFoldersUseCase
import com.example.kairos_mobile.domain.usecase.folder.RenameFolderUseCase
import com.example.kairos_mobile.util.MainDispatcherRule
import com.example.kairos_mobile.util.TestFixtures
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
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

/**
 * NotesViewModel 단위 테스트
 * 폴더 기반 노트 관리 로직 검증
 */
@OptIn(ExperimentalCoroutinesApi::class)
class NotesViewModelTest {

    @get:Rule
    val dispatcherRule = MainDispatcherRule()

    private lateinit var getAllFoldersUseCase: GetAllFoldersUseCase
    private lateinit var createFolderUseCase: CreateFolderUseCase
    private lateinit var deleteFolderUseCase: DeleteFolderUseCase
    private lateinit var renameFolderUseCase: RenameFolderUseCase
    private lateinit var noteRepository: NoteRepository

    @Before
    fun setUp() {
        getAllFoldersUseCase = mockk()
        createFolderUseCase = mockk()
        deleteFolderUseCase = mockk()
        renameFolderUseCase = mockk()
        noteRepository = mockk()
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    /** 기본 폴더/노트 수 Flow 설정 후 ViewModel 생성 헬퍼 */
    private fun createViewModel(
        folders: List<Folder> = emptyList(),
        countMap: Map<String, Int> = emptyMap()
    ): NotesViewModel {
        every { getAllFoldersUseCase() } returns flowOf(folders)
        every { noteRepository.getFolderNoteCounts() } returns flowOf(countMap)
        return NotesViewModel(
            getAllFoldersUseCase,
            createFolderUseCase,
            deleteFolderUseCase,
            renameFolderUseCase,
            noteRepository
        )
    }

    // ========== 폴더 로딩 테스트 ==========

    @Test
    fun `init_loads_folders_with_counts`() = runTest {
        // Given: 2개 폴더, 노트 수 맵
        val folders = listOf(
            TestFixtures.folder(id = "f1", name = "아이디어", type = FolderType.IDEAS),
            TestFixtures.folder(id = "f2", name = "내 폴더", type = FolderType.USER)
        )
        val countMap = mapOf("f1" to 3, "f2" to 5)

        // When: ViewModel 생성
        val viewModel = createViewModel(folders, countMap)
        advanceUntilIdle()

        // Then: combine 결과가 uiState에 반영
        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertEquals(2, state.folders.size)
        assertEquals(3, state.folders[0].noteCount)
        assertEquals(5, state.folders[1].noteCount)
    }

    @Test
    fun `inbox_hidden_when_count_zero`() = runTest {
        // Given: INBOX 폴더, 노트 0개
        val inbox = TestFixtures.folder(id = "inbox", name = "Inbox", type = FolderType.INBOX)
        val countMap = mapOf("inbox" to 0)

        // When
        val viewModel = createViewModel(listOf(inbox), countMap)
        advanceUntilIdle()

        // Then: INBOX가 목록에서 제외됨
        assertTrue(viewModel.uiState.value.folders.isEmpty())
    }

    @Test
    fun `inbox_shown_when_count_positive`() = runTest {
        // Given: INBOX 폴더, 노트 2개
        val inbox = TestFixtures.folder(id = "inbox", name = "Inbox", type = FolderType.INBOX)
        val countMap = mapOf("inbox" to 2)

        // When
        val viewModel = createViewModel(listOf(inbox), countMap)
        advanceUntilIdle()

        // Then: INBOX가 목록에 포함됨
        assertEquals(1, viewModel.uiState.value.folders.size)
        assertEquals(FolderType.INBOX, viewModel.uiState.value.folders[0].folder.type)
    }

    @Test
    fun `ideas_bookmarks_always_visible`() = runTest {
        // Given: IDEAS, BOOKMARKS 폴더 — 노트 0개
        val ideas = TestFixtures.folder(id = "ideas", name = "Ideas", type = FolderType.IDEAS)
        val bookmarks = TestFixtures.folder(id = "bm", name = "Bookmarks", type = FolderType.BOOKMARKS)
        val countMap = mapOf("ideas" to 0, "bm" to 0)

        // When
        val viewModel = createViewModel(listOf(ideas, bookmarks), countMap)
        advanceUntilIdle()

        // Then: 노트 0개여도 표시됨
        assertEquals(2, viewModel.uiState.value.folders.size)
    }

    // ========== 폴더 선택/뒤로가기 ==========

    @Test
    fun `selectFolder_loads_notes`() = runTest {
        // Given
        val folder = TestFixtures.folder(id = "f1", name = "내 폴더")
        val notePreview = NoteWithCapturePreview(
            noteId = "n1", captureId = "c1",
            aiTitle = "AI 제목", originalText = "원본 텍스트", createdAt = 1000L
        )
        every { noteRepository.getNotesWithActiveCaptureByFolderId("f1") } returns flowOf(listOf(notePreview))
        val viewModel = createViewModel()
        advanceUntilIdle()

        // When: 폴더 선택
        viewModel.onEvent(NotesEvent.SelectFolder(folder))
        advanceUntilIdle()

        // Then: selectedFolder 설정, 노트 로드됨
        val state = viewModel.uiState.value
        assertEquals(folder, state.selectedFolder)
        assertEquals(1, state.notes.size)
        assertEquals("n1", state.notes[0].noteId)
        assertEquals("AI 제목", state.notes[0].aiTitle)
    }

    @Test
    fun `backToFolders_clears_selection`() = runTest {
        // Given: 폴더가 선택된 상태
        val folder = TestFixtures.folder(id = "f1")
        every { noteRepository.getNotesWithActiveCaptureByFolderId("f1") } returns flowOf(emptyList())
        val viewModel = createViewModel()
        advanceUntilIdle()
        viewModel.onEvent(NotesEvent.SelectFolder(folder))
        advanceUntilIdle()

        // When: 뒤로가기
        viewModel.onEvent(NotesEvent.BackToFolders)
        advanceUntilIdle()

        // Then: selectedFolder = null, notes = empty
        val state = viewModel.uiState.value
        assertNull(state.selectedFolder)
        assertTrue(state.notes.isEmpty())
    }

    // ========== 폴더 CRUD ==========

    @Test
    fun `createFolder_success_closes_dialog`() = runTest {
        // Given
        coEvery { createFolderUseCase(any()) } just runs
        val viewModel = createViewModel()
        advanceUntilIdle()

        // 다이얼로그 열기
        viewModel.onEvent(NotesEvent.ShowCreateFolderDialog)

        // When: 폴더 생성
        viewModel.onEvent(NotesEvent.CreateFolder("새 폴더"))
        advanceUntilIdle()

        // Then: 다이얼로그 닫힘, UseCase 호출됨
        assertFalse(viewModel.uiState.value.showCreateFolderDialog)
        coVerify(exactly = 1) { createFolderUseCase("새 폴더") }
    }

    @Test
    fun `createFolder_error_sets_message`() = runTest {
        // Given: UseCase가 예외 발생
        coEvery { createFolderUseCase(any()) } throws RuntimeException("폴더 생성 실패")
        val viewModel = createViewModel()
        advanceUntilIdle()

        // When
        viewModel.onEvent(NotesEvent.CreateFolder("잘못된 폴더"))
        advanceUntilIdle()

        // Then: errorMessage 설정됨
        assertEquals("폴더 생성 실패", viewModel.uiState.value.errorMessage)
    }

    @Test
    fun `renameFolder_delegates`() = runTest {
        // Given
        coEvery { renameFolderUseCase(any(), any()) } just runs
        val viewModel = createViewModel()
        advanceUntilIdle()

        // When: 폴더 이름 변경
        viewModel.onEvent(NotesEvent.RenameFolder("f1", "새 이름"))
        advanceUntilIdle()

        // Then: UseCase 호출됨, renamingFolder null로 리셋
        coVerify(exactly = 1) { renameFolderUseCase("f1", "새 이름") }
        assertNull(viewModel.uiState.value.renamingFolder)
    }

    @Test
    fun `deleteFolder_delegates`() = runTest {
        // Given
        coEvery { deleteFolderUseCase(any()) } just runs
        val viewModel = createViewModel()
        advanceUntilIdle()

        // When: 폴더 삭제
        viewModel.onEvent(NotesEvent.DeleteFolder("f1"))
        advanceUntilIdle()

        // Then: UseCase 호출됨
        coVerify(exactly = 1) { deleteFolderUseCase("f1") }
    }
}
