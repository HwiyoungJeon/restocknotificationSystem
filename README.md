# 재입고 알림 시스템

## 프로젝트 소개
과제 재입고 알림 시스템은 상품이 재입고 되었을 때, 재입고 알림을 설정한 유저들에게 재입고 알림을 보내줍니다.

---

## 기술 스택
- **언어**: Java, Spring Boot
- **DB**: MySQL
- **빌드 도구**: Gradle
- **테스트**: JUnit5

---

## 비즈니스 요구 사항
- 재입고 알림을 전송하기 전, 상품의 재입고 회차를 1 증가 시킨다.
  - 실제 서비스에서는 다른 형태로 관리하지만, 과제에서는 직접 관리한다.
- 상품이 재입고 되었을 때, 재입고 알림을 설정한 유저들에게 알림 메시지를 전달해야 한다.
  - ProductUserNotification 테이블에 존재하는 유저는 모두 재입고 알림을 설정했다고 가정한다.
- 재입고 알림은 재입고 알림을 설정한 유저 순서대로 메시지를 전송한다.
- 회차별 재입고 알림을 받은 유저 목록을 저장해야 한다.
- 재입고 알림을 보내던 중 재고가 모두 없어진다면 알림 보내는 것을 중단합니다.
- 재입고 알림 전송의 상태를 DB 에 저장해야 한다.
  - IN_PROGRESS (발송 중)
  - CANCELED_BY_SOLD_OUT (품절에 의한 발송 중단)
  - CANCELED_BY_ERROR (예외에 의한 발송 중단)
    - 서드 파티 연동에서의 예외 를 의미한다.
  - COMPLETED (완료)

---

## **기술적 요구 사항**
- 알림 메시지는 1초에 최대 500개의 요청을 보낼 수 있다.
    - 서드 파티 연동을 하진 않고, ProductNotificationHistory 테이블에 데이터를 저장한다.
- Mysql 조회 시, 인덱스를 잘 탈 수 있게 설계해야 합니다.
- 설계해야 할 테이블 목록
    1. Product (상품)
        1. 상품 아이디
        2. 재입고 회차
        3. 재고 상태
    2. ProductNotificationHistory (상품별 재입고 알림 히스토리)
        1. 상품 아이디
        2. 재입고 회차
        3. 재입고 알림 발송 상태
        4. 마지막 발송 유저 아이디
    3. ProductUserNotification (상품별 재입고 알림을 설정한 유저)
        1. 상품 아이디
        2. 유저 아이디
        3. 활성화 여부
        4. 생성 날짜
        5. 수정 날짜
    4. ProductUserNotificationHistory (상품 + 유저별 알림 히스토리)
        1. 상품 아이디
        2. 유저 아이디
        3. 재입고 회차
        4. 발송 날짜
- (Optional) 예외에 의해 알림 메시지 발송이 실패한 경우, manual 하게 상품 재입고 알림 메시지를 다시 보내는 API를 호출한다면 마지막으로 전송 성공한 이후 유저부터 다시 알림 메시지를 보낼 수 있어야 한다.
    - 10번째 유저까지 알림 메시지 전송에 성공했다면, 다음 요청에서 11번째 유저부터 알림 메시지를 전송할 수 있어야 한다.
- 시스템 구조 상 비동기로 처리 되어야 하는 부분은 존재하지 않는다고 가정합니다.
- (Optional) 테스트 코드를 작성하면 좋습니다.

---

## 🔧 프로젝트 구조
    src/ 
      ├── docker/ # Docker 관련 설정 
      ├── main/ 
      │ ├── java/com/example/restocknotificationsysyem/ │ 
      │ ├── domain/ # 도메인 관련 계층 │ 
      │ │ ├── controller/ # 컨트롤러 계층 │ 
      │ │ ├── dto/ # 데이터 전송 객체 (DTO) │ 
      │ │ ├── entity/ # 엔티티 클래스 │ 
      │ │ ├── repository/ # 데이터베이스 접근 계층 │ 
      │ │ ├── service/ # 서비스 로직 계층 │ 
      │ ├── global/ # 전역 설정 및 관리 │ 
      │ │ ├── exception/ # 예외 처리 │ 
      │ │ ├── message/ # 메시지 관리 │ 
      │ │ └── scheduler/ # 스케줄러 관련 │ 
      │ └── RestockNotificationSysyemApplication # 메인 애플리케이션 실행 파일
      │ └── resources/ │ 
      ├── application.properties # 애플리케이션 설정 파일 
      └── test/ 
      └── java/com/example/restocknotificationsysyem/ 
      └── domain/service/ # 서비스 계층 테스트
---




