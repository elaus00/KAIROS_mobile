package com.flit.app.domain.usecase.classification

import com.flit.app.domain.model.ClassificationPreset
import javax.inject.Inject

/** 분류 프리셋 목록 조회 */
class GetPresetsUseCase @Inject constructor() {
    operator fun invoke(): List<ClassificationPreset> {
        return listOf(
            ClassificationPreset("default", "기본", "일반적인 분류 기준"),
            ClassificationPreset("work", "업무", "업무 중심 분류 (회의, 프로젝트, 데드라인)"),
            ClassificationPreset("personal", "개인", "개인 생활 중심 분류 (약속, 쇼핑, 취미)"),
            ClassificationPreset("study", "학습", "학습 중심 분류 (강의, 과제, 참고자료)")
        )
    }
}
