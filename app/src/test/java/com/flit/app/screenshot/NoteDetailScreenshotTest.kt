package com.flit.app.screenshot

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import com.flit.app.domain.model.ClassifiedType
import com.flit.app.domain.model.Folder
import com.flit.app.domain.model.FolderType
import com.flit.app.domain.model.NoteDetail
import com.flit.app.presentation.notes.detail.NoteDetailContent
import com.flit.app.presentation.notes.detail.NoteDetailUiState
import com.flit.app.ui.theme.FlitTheme
import com.github.takahirom.roborazzi.captureRoboImage
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.GraphicsMode

/**
 * NoteDetailScreen 스크린샷 테스트
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class NoteDetailScreenshotTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun noteDetailContent_default() {
        composeRule.setContent {
            FlitTheme {
                NoteDetailContent(
                    uiState = NoteDetailUiState(
                        isLoading = false,
                        noteDetail = NoteDetail(
                            noteId = "note1",
                            captureId = "capture1",
                            aiTitle = "샘플 노트",
                            originalText = "원본 텍스트입니다.",
                            body = "이것은 노트 본문입니다.\n여러 줄로 작성된 내용입니다.",
                            classifiedType = ClassifiedType.NOTES,
                            noteSubType = null,
                            folderId = "folder1",
                            imageUri = null,
                            tags = listOf("태그1", "태그2"),
                            createdAt = System.currentTimeMillis() - 86400000,
                            updatedAt = System.currentTimeMillis()
                        ),
                        editedTitle = "샘플 노트",
                        editedBody = "이것은 노트 본문입니다.\n여러 줄로 작성된 내용입니다.",
                        selectedFolderId = "folder1",
                        folders = listOf(
                            Folder(id = "folder1", name = "업무", type = FolderType.USER),
                            Folder(id = "folder2", name = "개인", type = FolderType.USER)
                        )
                    ),
                    onNavigateBack = {},
                    onShare = {},
                    onDelete = {},
                    onRetry = {},
                    onTitleChanged = {},
                    onBodyChanged = {},
                    onToggleOriginalText = {},
                    onFolderChanged = {},
                    onUndoDelete = {}
                )
            }
        }
        composeRule.onRoot().captureRoboImage("src/test/screenshots/note_detail_default.png")
    }

    @Test
    fun noteDetailContent_withImage() {
        composeRule.setContent {
            FlitTheme {
                NoteDetailContent(
                    uiState = NoteDetailUiState(
                        isLoading = false,
                        noteDetail = NoteDetail(
                            noteId = "note2",
                            captureId = "capture2",
                            aiTitle = "이미지 포함 노트",
                            originalText = "원본 텍스트입니다.",
                            body = "이미지가 첨부된 노트입니다.",
                            classifiedType = ClassifiedType.NOTES,
                            noteSubType = null,
                            folderId = "folder1",
                            imageUri = "content://media/sample.jpg",
                            tags = listOf("이미지"),
                            createdAt = System.currentTimeMillis() - 86400000,
                            updatedAt = System.currentTimeMillis()
                        ),
                        editedTitle = "이미지 포함 노트",
                        editedBody = "이미지가 첨부된 노트입니다.",
                        selectedFolderId = "folder1",
                        folders = listOf(
                            Folder(id = "folder1", name = "업무", type = FolderType.USER)
                        )
                    ),
                    onNavigateBack = {},
                    onShare = {},
                    onDelete = {},
                    onRetry = {},
                    onTitleChanged = {},
                    onBodyChanged = {},
                    onToggleOriginalText = {},
                    onFolderChanged = {},
                    onUndoDelete = {}
                )
            }
        }
        composeRule.onRoot().captureRoboImage("src/test/screenshots/note_detail_with_image.png")
    }

    @Test
    fun noteDetailContent_emptyState() {
        composeRule.setContent {
            FlitTheme {
                NoteDetailContent(
                    uiState = NoteDetailUiState(
                        isLoading = false,
                        noteDetail = NoteDetail(
                            noteId = "note3",
                            captureId = "capture3",
                            aiTitle = "",
                            originalText = "",
                            body = "",
                            classifiedType = ClassifiedType.NOTES,
                            noteSubType = null,
                            folderId = "folder1",
                            imageUri = null,
                            tags = emptyList(),
                            createdAt = System.currentTimeMillis(),
                            updatedAt = System.currentTimeMillis()
                        ),
                        editedTitle = "",
                        editedBody = "",
                        selectedFolderId = "folder1",
                        folders = listOf(
                            Folder(id = "folder1", name = "받은함", type = FolderType.INBOX)
                        )
                    ),
                    onNavigateBack = {},
                    onShare = {},
                    onDelete = {},
                    onRetry = {},
                    onTitleChanged = {},
                    onBodyChanged = {},
                    onToggleOriginalText = {},
                    onFolderChanged = {},
                    onUndoDelete = {}
                )
            }
        }
        composeRule.onRoot().captureRoboImage("src/test/screenshots/note_detail_empty.png")
    }

    @Test
    fun noteDetailContent_loading() {
        composeRule.setContent {
            FlitTheme {
                NoteDetailContent(
                    uiState = NoteDetailUiState(isLoading = true),
                    onNavigateBack = {},
                    onShare = {},
                    onDelete = {},
                    onRetry = {},
                    onTitleChanged = {},
                    onBodyChanged = {},
                    onToggleOriginalText = {},
                    onFolderChanged = {},
                    onUndoDelete = {}
                )
            }
        }
        composeRule.onRoot().captureRoboImage("src/test/screenshots/note_detail_loading.png")
    }
}
