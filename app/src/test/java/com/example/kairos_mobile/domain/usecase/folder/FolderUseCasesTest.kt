package com.example.kairos_mobile.domain.usecase.folder

import com.example.kairos_mobile.domain.model.FolderType
import com.example.kairos_mobile.domain.repository.FolderRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class FolderUseCasesTest {

    @Test
    fun createFolder_rejects_blank_name() = runTest {
        val repository = mockk<FolderRepository>()
        val useCase = CreateFolderUseCase(repository)

        val error = runCatching { useCase(" ") }.exceptionOrNull()

        assertTrue(error is IllegalArgumentException)
        assertEquals("폴더 이름이 비어있습니다", error?.message)
        coVerify(exactly = 0) { repository.createFolder(any()) }
    }

    @Test
    fun createFolder_rejects_duplicate_name() = runTest {
        val repository = mockk<FolderRepository>()
        val useCase = CreateFolderUseCase(repository)

        coEvery { repository.existsByName("Ideas") } returns true

        val error = runCatching { useCase("Ideas") }.exceptionOrNull()

        assertTrue(error is IllegalArgumentException)
        assertEquals("이미 존재하는 폴더 이름입니다", error?.message)
        coVerify(exactly = 0) { repository.createFolder(any()) }
    }

    @Test
    fun createFolder_creates_user_folder_when_valid() = runTest {
        val repository = mockk<FolderRepository>()
        val useCase = CreateFolderUseCase(repository)

        coEvery { repository.existsByName("Ideas") } returns false
        coEvery { repository.createFolder(any()) } just runs

        useCase("Ideas")

        coVerify(exactly = 1) {
            repository.createFolder(match {
                it.name == "Ideas" && it.type == FolderType.USER
            })
        }
    }

    @Test
    fun renameFolder_validates_and_delegates() = runTest {
        val repository = mockk<FolderRepository>()
        val useCase = RenameFolderUseCase(repository)

        coEvery { repository.existsByName("Archive") } returns false
        coEvery { repository.renameFolder("f1", "Archive") } just runs

        useCase("f1", "Archive")

        coVerify(exactly = 1) { repository.renameFolder("f1", "Archive") }
    }

    @Test
    fun renameFolder_rejects_blank_and_duplicate_name() = runTest {
        val repository = mockk<FolderRepository>()
        val useCase = RenameFolderUseCase(repository)

        val blankError = runCatching { useCase("f1", " ") }.exceptionOrNull()
        assertTrue(blankError is IllegalArgumentException)
        assertEquals("폴더 이름이 비어있습니다", blankError?.message)

        coEvery { repository.existsByName("Inbox") } returns true
        val duplicateError = runCatching { useCase("f1", "Inbox") }.exceptionOrNull()
        assertTrue(duplicateError is IllegalArgumentException)
        assertEquals("이미 존재하는 폴더 이름입니다", duplicateError?.message)

        coVerify(exactly = 0) { repository.renameFolder(any(), any()) }
    }

    // ── 폴더 이름 30자 제한 테스트 ──

    @Test
    fun createFolder_rejects_name_longer_than_30_chars() = runTest {
        val repository = mockk<FolderRepository>()
        val useCase = CreateFolderUseCase(repository)

        val error = runCatching { useCase("a".repeat(31)) }.exceptionOrNull()

        assertTrue(error is IllegalArgumentException)
        assertEquals("폴더 이름은 30자 이내여야 합니다", error?.message)
        coVerify(exactly = 0) { repository.createFolder(any()) }
    }

    @Test
    fun createFolder_accepts_name_at_30_chars() = runTest {
        val repository = mockk<FolderRepository>()
        val useCase = CreateFolderUseCase(repository)

        coEvery { repository.existsByName(any()) } returns false
        coEvery { repository.createFolder(any()) } just runs

        useCase("a".repeat(30))

        coVerify(exactly = 1) { repository.createFolder(any()) }
    }

    @Test
    fun renameFolder_rejects_name_longer_than_30_chars() = runTest {
        val repository = mockk<FolderRepository>()
        val useCase = RenameFolderUseCase(repository)

        val error = runCatching { useCase("f1", "b".repeat(31)) }.exceptionOrNull()

        assertTrue(error is IllegalArgumentException)
        assertEquals("폴더 이름은 30자 이내여야 합니다", error?.message)
        coVerify(exactly = 0) { repository.renameFolder(any(), any()) }
    }

    @Test
    fun renameFolder_accepts_name_at_30_chars() = runTest {
        val repository = mockk<FolderRepository>()
        val useCase = RenameFolderUseCase(repository)

        coEvery { repository.existsByName(any()) } returns false
        coEvery { repository.renameFolder("f1", any()) } just runs

        useCase("f1", "b".repeat(30))

        coVerify(exactly = 1) { repository.renameFolder("f1", any()) }
    }
}
