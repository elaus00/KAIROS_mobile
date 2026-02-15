# Glance Todo 위젯 토글 불일치 디버깅 기록

**날짜**: 2026-02-15  
**컨텍스트**: 홈 화면 `오늘 할 일` 위젯에서 체크/해제 반영이 간헐적으로 실패

## 문제 요약

- 위젯에서 할 일을 체크하면 DB는 바뀌는데 UI가 즉시 반영되지 않거나, 해제가 안 되는 것처럼 보임
- `updateAll()`은 호출되는데도 렌더링이 기대대로 갱신되지 않음

## 초기 가설과 한계

### 가설 1: SQL 토글 문제
- `UPDATE todos SET is_completed = NOT is_completed` 쿼리 자체는 정상
- 실제 DB 상태도 토글마다 정확히 반전됨

### 가설 2: `itemId` 캐시 문제
- `itemId`에 `isCompleted`를 포함해 변경 강제 (`"${todoId}_${isCompleted}"`)
- 일부 케이스 개선은 있었지만, 근본적으로 불안정 증상은 남음

핵심: `itemId` 개선은 필요 조건일 수 있지만 충분 조건은 아니었음.

## 추적 방식 (Trace ID 기반)

클릭 1회당 `traceId`를 생성하고 아래 전 구간 로그를 연결:

1. `ToggleTodoAction.onAction` 진입/종료
2. UseCase 진입/종료
3. Repository 토글 전/후 DB 단건 상태
4. 토글 전/후 위젯 쿼리 스냅샷
5. `updateAll()` 대상 (`glanceIds`, `appWidgetIds`)과 소요 시간
6. `provideGlance` 호출 여부 및 로드 데이터

추가로 `TodoWidgetReceiver`의 `onReceive/onUpdate`도 기록해 브로드캐스트 전달 여부 확인.

## 결정적 관찰

로그에서 다음은 항상 성립:

- `onAction` 매번 정상 진입
- `afterToggle`의 `done` 값이 매번 정확히 반전
- `updateAll()`/`updateAppWidget()` 매번 호출
- 위젯 인스턴스 매핑도 정상 (`appWidgetId=31`)

그런데 **`provideGlance`는 첫 토글에서만 보이고, 이후 토글에서는 재호출이 안 되는 구간이 존재**.

즉, 문제는 "토글/DB/업데이트 호출 실패"가 아니라, **세션 중 데이터 공급 방식**에 있었음.

## 근본 원인

`provideGlance`에서 DB를 1회 조회한 스냅샷 리스트를 `provideContent`에 전달하고 있었음.

- Glance 세션이 유지되면 `provideGlance`가 매 토글마다 다시 호출된다는 보장이 없음
- 이 구조에서는 세션 내부 컴포지션이 오래된 리스트를 계속 참조할 수 있음
- 결과적으로 DB는 바뀌어도 UI가 stale 상태로 남는 구간이 발생

정리하면:
`updateAll()`은 "갱신 요청"이고, 세션 내 "데이터 동기화"는 별도 상태 구독으로 보장해야 함.

## 최종 해결

### 1) 위젯용 Flow DAO 추가
- `TodoDao.observeTodayTodosForWidget(todayEndMs): Flow<List<TodoWithCaptureRow>>`

### 2) `provideContent` 내부에서 Flow 구독
- `collectAsState(initial = emptyList())`로 데이터 반응형 구독
- 세션이 유지되어도 DB 변경 시 리스트가 즉시 재방출되어 UI 갱신

### 3) `itemId`에 완료 상태 포함 유지
- `itemId = { "${it.todoId}_${it.isCompleted}".hashCode().toLong() }`
- 상태 변화를 RemoteViews diff에서 안정적으로 반영

## 수정 파일

- `app/src/main/java/com/flit/app/data/local/database/dao/TodoDao.kt`
- `app/src/main/java/com/flit/app/presentation/widget/TodoGlanceWidget.kt`
- (추적 로그)  
  `app/src/main/java/com/flit/app/domain/usecase/todo/ToggleTodoCompletionUseCase.kt`  
  `app/src/main/java/com/flit/app/data/repository/TodoRepositoryImpl.kt`  
  `app/src/main/java/com/flit/app/presentation/widget/WidgetUpdateHelper.kt`

## 재발 방지 체크리스트

1. Glance는 `provideGlance` 재호출을 전제로 설계하지 않는다.
2. 세션 내 변경 가능 데이터는 `provideContent`에서 `Flow/State`로 구독한다.
3. 리스트형 위젯은 `itemId`에 실제 렌더링 상태를 반영한다.
4. 위젯 이슈는 클릭-DB-업데이트-렌더 경로를 같은 `traceId`로 묶어 본다.
