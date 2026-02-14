#!/bin/bash
# Claude hook: 프롬프트 제출 시 브랜치에 맞는 Build Variant 자동 설정

BRANCH_NAME=$(git rev-parse --abbrev-ref HEAD 2>/dev/null)
GRADLE_XML=".idea/gradle.xml"

# Git 리포지토리가 아니면 종료
if [ -z "$BRANCH_NAME" ]; then
    exit 0
fi

# .idea/gradle.xml이 없으면 종료 (Android Studio 프로젝트가 아님)
if [ ! -f "$GRADLE_XML" ]; then
    exit 0
fi

# 브랜치에 따라 Build Variant 결정
case "$BRANCH_NAME" in
    develop)
        VARIANT="devDebug"
        ;;
    release/*)
        VARIANT="stagingRelease"
        ;;
    master|main)
        VARIANT="productionRelease"
        ;;
    *)
        # feature/* 등은 devDebug 사용
        VARIANT="devDebug"
        ;;
esac

# 현재 설정된 Build Variant 확인
CURRENT_VARIANT=$(grep -o 'selectedBuildVariant="app:[^"]*"' "$GRADLE_XML" 2>/dev/null | sed 's/selectedBuildVariant="app:\([^"]*\)"/\1/')

# 이미 올바른 Variant라면 종료
if [ "$CURRENT_VARIANT" = "$VARIANT" ]; then
    exit 0
fi

# Build Variant 변경
if grep -q "selectedBuildVariant" "$GRADLE_XML"; then
    sed -i '' "s/selectedBuildVariant=\"app:[^\"]*\"/selectedBuildVariant=\"app:$VARIANT\"/" "$GRADLE_XML"
    echo "🔄 Build Variant 자동 전환: $CURRENT_VARIANT → $VARIANT (브랜치: $BRANCH_NAME)"
fi
