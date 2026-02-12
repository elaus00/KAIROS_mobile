package com.flit.app.data.repository

import android.content.Context
import android.net.Uri
import com.flit.app.domain.repository.ImageRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

/**
 * ImageRepository 구현체
 * 이미지를 앱 내부 디렉토리(captures/)에 복사
 */
@Singleton
class ImageRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : ImageRepository {

    private val capturesDir: File
        get() = File(context.filesDir, "captures").also { it.mkdirs() }

    override suspend fun saveImage(sourceUri: Uri): String {
        val fileName = "img_${UUID.randomUUID()}.jpg"
        val destFile = File(capturesDir, fileName)

        context.contentResolver.openInputStream(sourceUri)?.use { input ->
            destFile.outputStream().use { output ->
                input.copyTo(output)
            }
        } ?: throw IllegalStateException("이미지를 읽을 수 없습니다")

        return Uri.fromFile(destFile).toString()
    }

    override suspend fun deleteImage(imageUri: String) {
        try {
            val uri = Uri.parse(imageUri)
            val path = uri.path ?: return
            val file = File(path)
            if (file.exists()) {
                file.delete()
            }
        } catch (_: Exception) {
            // 삭제 실패 무시
        }
    }
}
