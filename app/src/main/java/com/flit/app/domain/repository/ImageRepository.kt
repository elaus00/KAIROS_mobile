package com.flit.app.domain.repository

import android.net.Uri

/**
 * 이미지 저장소 인터페이스
 * 갤러리/카메라에서 선택된 이미지를 앱 내부 디렉토리에 저장
 */
interface ImageRepository {
    /** 이미지를 앱 내부 디렉토리에 복사하고 URI 반환 */
    suspend fun saveImage(sourceUri: Uri): String
    /** 저장된 이미지 삭제 */
    suspend fun deleteImage(imageUri: String)
}
