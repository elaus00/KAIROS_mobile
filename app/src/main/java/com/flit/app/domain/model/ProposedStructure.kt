package com.flit.app.domain.model

/** AI 재구성 제안 구조 */
data class ProposedStructure(
    val folderName: String,
    val folderType: String,
    val action: String?,
    val captureIds: List<String>
)
