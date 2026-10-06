# 사진 개인정보 가리기

로그인 없이 사진에서 공개하고 싶지 않은 부분을 검은 사각형으로 가리고 저장·공유하는 Android 앱이다.

**사진 선택 → 가리기 → 결과 확인**

## 구현한 기능

- 시스템 사진 선택기로 한 장 가져오기
- 불투명한 사각형 추가·선택·이동·크기 변경·삭제
- 기존 박스를 바로 끌어 이동, 얇은 박스의 이동·모서리 조정 구분
- 박스 추가·크기 조정 중 경계를 확인하는 임시 2.5배 확대창
- 두 손가락 확대·이동, 실행 취소·다시 실행
- 실제 PNG 결과 미리보기, 사진첩 저장, Android 공유 메뉴
- 편집 유지와 버리기 확인, 화면 회전·작업 상태 복원
- 작은 화면·큰 글자용 버튼 배치와 가로 화면의 사진·조작부 배치
- 시스템 프로세스 종료 후 박스·변경 이력·선택·확대 상태 복원
- EXIF 방향 보정, 큰 사진 크기 제한, 원본 메타데이터 제거
- 사진 선택 화면에 테스트 배너, 필요한 광고 동의 처리

## 디자인 v2

아이보리·차콜·라임 색상으로 홈 구성, 사각형 행동 버튼, 편집 도구, 단계 표시, 확인 창과 앱 아이콘을 통일했다. Galaxy A34에 업데이트 설치했고 단위 테스트 45개, 실기기·에뮬레이터 테스트 각 19개를 통과했다.

## 문서

- [PRD](PRD.md)
- [디자인](DESIGN.md)
- [개발 계획](DEVELOPMENT_PLAN.md)
- [첨부 Android 스킬 적용 구조](ARCHITECTURE.md)
- [실기기·디자인 검증 결과](TEST_REPORT.md)

## 개발 환경과 실행

Android Studio가 제공하는 JDK를 사용해 Gradle을 실행한다. 프로젝트의 기존 SDK 설정은 minSdk 26, compileSdk / targetSdk 37이다.

```shell
./gradlew :app:assembleDebug
./gradlew :app:testDebugUnitTest :app:lintDebug
./gradlew :app:connectedDebugAndroidTest
./gradlew :app:assembleRelease
```

기기 테스트에는 실행 중인 에뮬레이터 또는 연결한 Android 기기가 필요하다. 디버그 APK는 `app/build/outputs/apk/debug/app-debug.apk`에 생성된다. 릴리스 APK에는 아직 배포용 서명을 설정하지 않았다.

## 광고와 배포 준비

디버그에는 테스트 광고만 표시한다. 실제 AdMob ID가 없어 릴리스 광고는 비활성화되어 있다. 스토어 출시에는 실제 광고 설정, 개인정보 처리방침, 스토어 자료, 배포용 서명과 앱 번들이 필요하다.

Android 8~9에서는 시스템 파일 저장 화면을 사용한다. Android 10 이상에서는 사진첩에 저장한다. 현재 기기 검증 범위와 결과는 작업 보고에 기록한다.
