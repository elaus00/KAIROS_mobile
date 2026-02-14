# SQLite `INSERT OR REPLACE`의 참조 무결성 함정

**날짜**: 2026-02-15
**컨텍스트**: 버튼 크기 조정 및 시스템 폴더 이름 변경 작업

## 문제 상황

시스템 폴더 이름을 '받은함' → '인박스'로 업데이트하기 위해 `INSERT OR REPLACE`를 사용했더니, 기존 폴더에 속한 노트들이 모두 폴더 참조를 잃어버림.

### 발생한 현상
- UI에서 "인박스" 폴더가 완전히 사라짐
- 기존에 "받은함" 폴더에 속했던 노트들이 미분류 상태로 변경
- notes 테이블의 `folder_id` 참조가 깨짐

## 원인 분석

### SQLite `INSERT OR REPLACE`의 동작 방식

```sql
INSERT OR REPLACE INTO folders (id, name, type, sort_order, created_at)
VALUES ('system-inbox', '인박스', 'INBOX', 0, $now)
```

**실제 실행 과정**:
1. 기존 `id='system-inbox'` row를 **DELETE**
2. 새로운 row를 **INSERT**

→ 이것은 **DELETE + INSERT**이지, UPDATE가 아님!

### 외래키 참조 문제

```kotlin
// notes 테이블
@ColumnInfo(name = "folder_id")
val folderId: String?  // 'system-inbox' 참조
```

1. `DELETE` 시점에 기존 폴더 삭제
2. notes의 `folder_id='system-inbox'` 참조가 무효화됨
3. 외래키 제약조건에 따라:
   - `ON DELETE CASCADE`: notes도 함께 삭제 (최악)
   - `ON DELETE SET NULL`: folder_id가 NULL로 변경
   - 제약조건 없음: 참조는 남지만 실제 폴더는 없음 → 고아 레코드

## 올바른 해결 방법

### 방법 1: `INSERT OR IGNORE` + `UPDATE` (채택)

```kotlin
// 1. 폴더가 없으면 생성, 있으면 유지
db.execSQL(
    """
    INSERT OR IGNORE INTO folders (id, name, type, sort_order, created_at)
    VALUES ('system-inbox', '인박스', 'INBOX', 0, $now)
    """.trimIndent()
)

// 2. 기존 폴더 이름 업데이트
db.execSQL("UPDATE folders SET name = '인박스' WHERE id = 'system-inbox'")
```

**장점**:
- 기존 폴더 row 유지 → 참조 안 깨짐
- 이름만 업데이트
- created_at 등 다른 필드 보존

### 방법 2: Room Migration 사용

```kotlin
val MIGRATION_X_Y = object : Migration(X, Y) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("UPDATE folders SET name = '인박스' WHERE id = 'system-inbox'")
    }
}
```

**장점**:
- 버전 관리 명확
- 한 번만 실행됨

## 교훈

### `INSERT OR REPLACE` 사용 시 주의사항

1. **다른 테이블에서 참조되는 데이터는 절대 REPLACE 금지**
   - 외래키 참조가 깨질 수 있음
   - 대신 `UPDATE` 사용

2. **REPLACE = DELETE + INSERT**
   - 단순 업데이트가 아님
   - 모든 필드가 새로운 값으로 대체됨
   - AUTOINCREMENT는 재설정됨

3. **적절한 사용 사례**
   - 캐시 데이터 갱신 (참조 없음)
   - 설정 값 저장 (다른 테이블과 관계 없음)
   - 임시 데이터

### 대안 선택 가이드

| 상황 | 사용할 것 | 피할 것 |
|------|----------|---------|
| 이름/설명 등 일부 필드만 변경 | `UPDATE` | `INSERT OR REPLACE` |
| 외래키로 참조되는 데이터 | `UPDATE` | `INSERT OR REPLACE` |
| 참조 없는 캐시/설정 데이터 | `INSERT OR REPLACE` | - |
| 없으면 생성, 있으면 유지 | `INSERT OR IGNORE` | - |

## 관련 코드

- `FlitDatabase.kt:86-110` - `seedSystemFolders()` 함수
- `FolderEntity.kt` - folders 테이블 정의
- `NoteEntity.kt` - notes.folder_id 외래키 참조
