package com.flit.app.presentation.notes

import com.flit.app.domain.model.Folder
import com.flit.app.domain.model.FolderType
import com.flit.app.domain.model.NoteViewType
import com.flit.app.domain.model.NoteWithCapturePreview
import com.flit.app.domain.model.SubscriptionTier
import com.flit.app.domain.repository.CaptureRepository
import com.flit.app.domain.repository.FolderRepository
import com.flit.app.domain.repository.NoteRepository
import com.flit.app.domain.repository.SubscriptionRepository
import com.flit.app.domain.repository.UserPreferenceRepository
import com.flit.app.domain.usecase.folder.CreateFolderUseCase
import com.flit.app.domain.usecase.folder.RenameFolderUseCase
import com.flit.app.domain.usecase.note.UpdateNoteUseCase
import com.flit.app.util.MainDispatcherRule
import com.flit.app.util.TestFixtures
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
 * 노트 우선 뷰 + 폴더 필터 로직 검증
 */
@OptIn(ExperimentalCoroutinesApi::class)
class NotesViewModelTest {

    @get:Rule
    val dispatcherRule = MainDispatcherRule()

    private lateinit var folderRepository: FolderRepository
    private lateinit var createFolderUseCase: CreateFolderUseCase
    private lateinit var renameFolderUseCase: RenameFolderUseCase
    private lateinit var noteRepository: NoteRepository
    private lateinit var subscriptionRepository: SubscriptionRepository
    private lateinit var captureRepository: CaptureRepository
    private lateinit var userPreferenceRepository: UserPreferenceRepository
    private lateinit var updateNoteUseCase: UpdateNoteUseCase

    @Before
    fun setUp() {
        folderRepository = mockk()
        createFolderUseCase = mockk()
        renameFolderUseCase = mockk()
        noteRepository = mockk()
        subscriptionRepository = mockk()
        captureRepository = mockk()
        userPreferenceRepository = mockk()
        updateNoteUseCase = mockk()
        every { subscriptionRepository.getCachedTier() } returns SubscriptionTier.FREE
        every { userPreferenceRepository.observeString(any(), any()) } returns flowOf(NoteViewType.LIST.name)
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    /** 기본 폴더/노트 수/전체 노트 Flow 설정 후 ViewModel 생성 헬퍼 */
    private fun createViewModel(
        folders: List<Folder> = emptyList(),
        countMap: Map<String, Int> = emptyMap(),
        allNotes: List<NoteWithCapturePreview> = emptyList()
    ): NotesViewModel {
        every { folderRepository.getAllFolders() } returns flowOf(folders)
        every { noteRepository.getFolderNoteCounts() } returns flowOf(countMap)
        every { noteRepository.getAllNotesWithActiveCapture() } returns flowOf(allNotes)
        return NotesViewModel(
            folderRepository,
            createFolderUseCase,
            renameFolderUseCase,
            noteRepository,
            subscriptionRepository,
            captureRepository,
            userPreferenceRepository,
            updateNoteUseCase
        )
    }

    // ========== 데이터 로딩 테스트 ==========

    @Test
    fun `init_loads_folders_and_notes`() = runTest {
        // Given: 2개 폴더, 노트 수 맵, 전체 노트 2개
        val folders = listOf(
            TestFixtures.folder(id = "f1", name = "아이디어", type = FolderType.IDEAS),
            TestFixtures.folder(id = "f2", name = "내 폴더", type = FolderType.USER)
        )
        val countMap = mapOf("f1" to 3, "f2" to 5)
        val notes = listOf(
            NoteWithCapturePreview(
                noteId = "n1", captureId = "c1",
                aiTitle = "노트1", originalText = "원본1",
                createdAt = 1000L, folderId = "f1"
            ),
            NoteWithCapturePreview(
                noteId = "n2", captureId = "c2",
                aiTitle = "노트2", originalText = "원본2",
                createdAt = 2000L, folderId = "f2"
            )
        )

        // When: ViewModel 생성
        val viewModel = createViewModel(folders, countMap, notes)
        advanceUntilIdle()

        // Then: 폴더와 전체 노트가 로드됨
        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertEquals(2, state.folders.size)
        assertEquals(3, state.folders[0].noteCount)
        assertEquals(2, state.notes.size)
        assertNull(state.selectedFilterFolderId)
    }

    @Test
    fun `inbox_hidden_when_count_zero`() = runTest {
        // Given: INBOX 폴더, 노트 0개
        val inbox = TestFixtures.folder(id = "inbox", name = "받은함", type = FolderType.INBOX)
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
        val inbox = TestFixtures.folder(id = "inbox", name = "받은함", type = FolderType.INBOX)
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
        val ideas = TestFixtures.folder(id = "ideas", name = "아이디어", type = FolderType.IDEAS)
        val bookmarks = TestFixtures.folder(id = "bm", name = "북마크", type = FolderType.BOOKMARKS)
        val countMap = mapOf("ideas" to 0, "bm" to 0)

        // When
        val viewModel = createViewModel(listOf(ideas, bookmarks), countMap)
        advanceUntilIdle()

        // Then: 노트 0개여도 표시됨
        assertEquals(2, viewModel.uiState.value.folders.size)
    }

    // ========== 필터 테스트 ==========

    @Test
    fun `selectFilter_filters_notes_by_folder`() = runTest {
        // Given: 2개 폴더에 분산된 노트들
        val folders = listOf(
            TestFixtures.folder(id = "f1", name = "아이디어", type = FolderType.IDEAS),
            TestFixtures.folder(id = "f2", name = "내 폴더", type = FolderType.USER)
        )
        val countMap = mapOf("f1" to 2, "f2" to 1)
        val notes = listOf(
            NoteWithCapturePreview(
                noteId = "n1", captureId = "c1",
                aiTitle = "아이디어1", originalText = "원본1",
                createdAt = 1000L, folderId = "f1"
            ),
            NoteWithCapturePreview(
                noteId = "n2", captureId = "c2",
                aiTitle = "아이디어2", originalText = "원본2",
                createdAt = 2000L, folderId = "f1"
            ),
            NoteWithCapturePreview(
                noteId = "n3", captureId = "c3",
                aiTitle = "내 노트", originalText = "원본3",
                createdAt = 3000L, folderId = "f2"
            )
        )

        val viewModel = createViewModel(folders, countMap, notes)
        advanceUntilIdle()

        // When: f1 폴더 필터 선택
        viewModel.onEvent(NotesEvent.SelectFilter("f1"))
        advanceUntilIdle()

        // Then: f1 폴더 노트만 표시
        val state = viewModel.uiState.value
        assertEquals("f1", state.selectedFilterFolderId)
        assertEquals(2, state.notes.size)
        assertTrue(state.notes.all { it.folderId == "f1" })
    }

    @Test
    fun `selectFilter_null_shows_all_notes`() = runTest {
        // Given: 필터가 적용된 상태
        val folders = listOf(
            TestFixtures.folder(id = "f1", name = "폴더1", type = FolderType.IDEAS)
        )
        val notes = listOf(
            NoteWithCapturePreview(
                noteId = "n1", captureId = "c1",
                aiTitle = "노트1", originalText = "원본",
                createdAt = 1000L, folderId = "f1"
            ),
            NoteWithCapturePreview(
                noteId = "n2", captureId = "c2",
                aiTitle = "노트2", originalText = "원본2",
                createdAt = 2000L, folderId = "f2"
            )
        )

        val viewModel = createViewModel(folders, mapOf("f1" to 1), notes)
        advanceUntilIdle()
        viewModel.onEvent(NotesEvent.SelectFilter("f1"))
        advanceUntilIdle()

        // When: 전체 필터로 전환
        viewModel.onEvent(NotesEvent.SelectFilter(null))
        advanceUntilIdle()

        // Then: 모든 노트가 표시됨
        val state = viewModel.uiState.value
        assertNull(state.selectedFilterFolderId)
        assertEquals(2, state.notes.size)
    }

    @Test
    fun `notes_include_folder_names`() = runTest {
        // Given: 폴더에 속한 노트
        val folders = listOf(
            TestFixtures.folder(id = "f1", name = "아이디어", type = FolderType.IDEAS)
        )
        val notes = listOf(
            NoteWithCapturePreview(
                noteId = "n1", captureId = "c1",
                aiTitle = "노트1", originalText = "원본",
                createdAt = 1000L, folderId = "f1"
            )
        )

        // When
        val viewModel = createViewModel(folders, mapOf("f1" to 1), notes)
        advanceUntilIdle()

        // Then: 폴더 이름이 매핑됨
        assertEquals("아이디어", viewModel.uiState.value.notes[0].folderName)
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
        coEvery { folderRepository.deleteFolder(any()) } just runs
        val viewModel = createViewModel()
        advanceUntilIdle()

        // When: 폴더 삭제
        viewModel.onEvent(NotesEvent.DeleteFolder("f1"))
        advanceUntilIdle()

        // Then: UseCase 호출됨
        coVerify(exactly = 1) { folderRepository.deleteFolder("f1") }
    }

    @Test
    fun `deleteFolder_resets_filter_if_current`() = runTest {
        // Given: 필터가 f1으로 설정된 상태
        val folders = listOf(
            TestFixtures.folder(id = "f1", name = "내 폴더", type = FolderType.USER)
        )
        coEvery { folderRepository.deleteFolder(any()) } just runs
        val viewModel = createViewModel(folders, mapOf("f1" to 1))
        advanceUntilIdle()
        viewModel.onEvent(NotesEvent.SelectFilter("f1"))

        // When: 현재 필터 폴더를 삭제
        viewModel.onEvent(NotesEvent.DeleteFolder("f1"))
        advanceUntilIdle()

        // Then: 필터가 전체(null)로 리셋됨
        assertNull(viewModel.uiState.value.selectedFilterFolderId)
    }
}
