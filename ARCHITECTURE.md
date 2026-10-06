# 적용한 Android 스킬과 구현 구조

사용자가 지정한 `/Users/heojunseo/Desktop/dev/android-skills-master/skills`의 지침을 이 프로젝트에 적용했다.

## 적용 범위

| 스킬 | 적용 내용 |
| --- | --- |
| android-project-structure | 단일 앱 모듈, `presentation → domain ← data`, Android에 의존하지 않는 도메인 |
| android-presentation-mvi | 각 화면을 State / Action / Event / ViewModel / Screen / ScreenRoot로 분리 |
| android-di-hilt | Application·Activity·ViewModel에 Hilt 적용, Repository와 DataSource를 인터페이스로 바인딩 |
| android-navigation | Navigation 3, 직렬화 가능한 Route, 화면별 ViewModel·저장 상태 수명 관리 |
| android-data-layer | 로컬 DataSource에서 파일 처리, Repository에서 도메인 모델 변환, ViewModel은 UseCase로 접근 |
| android-error-handling | UseCase에서 예외를 타입이 있는 Result로 변환, 취소 예외는 다시 전달 |
| android-testing | 생성한 모든 UseCase·ViewModel의 성공·실패 테스트, 좌표·이력·실제 이미지 처리·Compose 조작 검증 |

스킬의 예시 코드 중 현재 Navigation 3에서 이름이 바뀐 상태 보관 decorator는 공식 API인 `rememberSaveableStateHolderNavEntryDecorator`로 적용했다. ViewModel decorator보다 먼저 등록한다. 서비스·네트워크·데이터베이스는 현재 제품에 필요하지 않아 관련 샘플 구조를 추가하지 않았다.

## 파일 배치

```text
com.devhjs.garim/
├── GarimApplication.kt
├── MainActivity.kt
├── core/
│   ├── ads/                   # 홈 배너, 필요한 광고 동의 처리
│   ├── di/                    # 인터페이스 Hilt 바인딩
│   ├── navigation/            # Route, NavDisplay
│   └── ui/                    # 도메인을 모르는 공용 UI
├── domain/
│   ├── error/                 # ImageError
│   ├── model/                 # 사진·결과·사각형·좌표·변경 이력·Result
│   ├── repository/            # PhotoRepository 인터페이스
│   └── usecase/               # 사진 가져오기·읽기·렌더링·결과 읽기·저장
├── data/
│   ├── datasource/local/      # 기기 이미지·파일·MediaStore 처리
│   ├── mapper/                # 파일 레코드 → 도메인 모델
│   └── repository/            # PhotoRepositoryImpl
├── presentation/
│   ├── component/             # 앱의 오류 메시지 변환
│   ├── home/                  # 사진 선택
│   ├── editor/                # 가리기
│   └── result/                # 결과 확인
└── ui/theme/                  # 디자인 테마
```

`Screen`은 State와 Action 콜백만 받는다. `ScreenRoot`가 사진 선택기·공유 메뉴·스낵바·뒤로 가기를 처리하고 ViewModel을 주입한다. 화면 간에 다른 화면의 ViewModel이나 State를 직접 참조하지 않는다.

편집 도중의 임시 사각형·포인터 좌표·정밀 확대창은 Canvas의 제스처 수명 동안만 유지한다. 확정된 사각형·선택·변경 이력·편집 확대 상태는 EditorState에서 관리한다. 결과 화면의 확대는 파일 내용을 바꾸지 않는 표시 상태다. 편집 확대창은 원본과 임시 박스를 표시하는 UI로, 결과 파일 렌더링에 전달되지 않는다.

## 사진 데이터 처리

### 작업 상태 복원

- Navigation 3의 백스택과 화면별 SavedStateHandle로 진행 중인 작업을 복원한다. 사진과 결과 파일은 로컬 DataSource에서 다시 읽는다.
- 편집 이력은 중복 사각형을 한 번만 기록한 JSON 스냅샷으로 보관한다. 현재 박스와 최대 50단계의 실행 취소·다시 실행 이력, 선택, 확대 배율·이동 비율을 저장한다.
- 이동 위치는 화면 픽셀 대신 확대된 사진 크기에 대한 비율이다. Canvas 크기가 달라지면 새 화면의 경계에 맞춰 표시한다.
- 결과 화면의 확대는 rememberSaveable로, 저장 성공 여부는 ResultViewModel의 SavedStateHandle로 복원한다. 처리 중·저장 중 상태는 재생성 시 초기화한다.
- 복원 기준은 Android가 보관한 동일 작업이다. 사용자가 최근 작업을 지우거나 강제 종료한 뒤 새로 시작하는 경우의 자동 초안 복구는 구현하지 않았다. 별도 작업 목록 화면은 제공하지 않는다.

1. 선택한 URI를 로컬 DataSource에서 읽고, 방향을 보정한 작업용 PNG를 앱의 백업 제외 영역에 저장한다.
2. 박스는 사진에 대한 정규화 좌표로 저장한다. 확대·축소는 표시 좌표만 바꾼다.
3. 결과 생성 시 박스를 사진 픽셀에 검은색으로 합성하고 별도 PNG를 만든다.
4. 결과 화면, 사진첩 저장, 공유 메뉴는 같은 결과 파일을 사용한다.
5. FileProvider는 `cache/exports`만 공유할 수 있다. 작업용 원본 영역은 포함하지 않는다.

작업용 사진의 보관 상한은 7일, 공유용 결과 캐시의 보관 상한은 1일이다. 만료 파일은 다음 사진 가져오기 때 정리한다. 갤러리에 저장한 결과는 앱이 정리하지 않는다. 원본 메타데이터는 새 PNG에 복사하지 않는다.

## 광고 설정

디버그 빌드는 Google의 테스트 앱 ID·배너 ID를 사용한다. 릴리스 빌드는 실제 광고 ID가 없으므로 배너를 비활성화한다. 정식 수익화를 시작하려면 AdMob ID, 배포 지역에 맞는 동의 설정, 개인정보 처리방침·스토어 데이터 보안 정보를 준비한 뒤 릴리스 광고 설정을 연결해야 한다.

## 검증 근거

- [Navigation 3 상태 보관](https://developer.android.com/guide/navigation/navigation-3/save-state)
- [Hilt Gradle 설정](https://dagger.dev/hilt/gradle-setup.html)
- 사진 선택·저장·공유·광고 관련 자료는 [개발 계획](DEVELOPMENT_PLAN.md)의 참고 문서를 따른다.
