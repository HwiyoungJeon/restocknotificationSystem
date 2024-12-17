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
## 설치 및 실행 방법

**1. 프로젝트 클론**  
```bash
git clone git@github.com:HwiyoungJeon/restocknotificationSystem.git
cd reviceservice
```
**2. Docker Compose 설치**  
```bash
sudo apt update
sudo apt install docker.io docker-compose -y
```
**3. Docker Compose-Redis 실행** 
```bash
docker-compose up -d
docker ps
docker exec -it {실행된 Redis 컨테이너이름} redis-cli
```
**4. 프로젝트 빌드 및 실행**  
```bash
./gradlew build
./gradlew bootRun
```

---
### **API 명세서**

**API 스펙**
```markdown
## API 명세서

### 1. **상품 재입고 및 알림 전송**
- **URL**: `POST /products/{productId}/notifications/re-stock`

### 2. **재입고 알림 전송 API(manual)**
- **URL**: `POST /admin/products/{productId}/notifications/re-stock`
```

### **성공 응답 예시**
```json
{
    "message": "정상적으로 실행 되었습니다.",
    "code": 200,
    "data": {
        "productId": 1,
        "stock": 96,
        "restockRound": 3,
        "restockTime": "2024-12-16 16:24:48",
        "notificationHistoryId": 7,
        "status": "COMPLETED",
        "notificationStartTime": "2024-12-16 16:24:43",
        "notificationEndTime": "2024-12-16 16:24:43",
        "lastSentUserId": 1,
        "notifiedUsers": [
            {
                "userId": 1,
                "notificationSent": true
            }
        ]
    }
}

```
## **API 응답 필드 상세 설명**

| **필드명**         | **타입**   | **설명**                                           |
|------------------|------------|--------------------------------------------------|
| **message**       | `String`   | API 호출에 대한 응답 메시지                         | 
| **code**          | `Integer`  | HTTP 상태 코드 (200 - OK)                         |
| **data**          | `Object`   | 호출 결과에 대한 실제 데이터                      |
| **productId**     | `Long`     | 재입고가 발생한 상품의 ID                          |
| **stock**         | `Integer`  | 재입고 후 남아있는 재고 수량                        |
| **restockRound**  | `Integer`  | 재입고 회차 (몇 번째 재입고인지)                    |
| **restockTime**   | `String`   | 재입고가 발생한 시간 (yyyy-MM-dd HH:mm:ss)          |
| **notificationHistoryId** | `Long` | 이번 재입고 알림과 관련된 알림 히스토리 ID         |
| **status**        | `String`   | 알림 상태 (IN_PROGRESS, COMPLETED, CANCELED_BY_SOLD_OUT, CANCELED_BY_ERROR) |
| **notificationStartTime** | `String` | 알림 발송이 시작된 시간 (yyyy-MM-dd HH:mm:ss)    |
| **notificationEndTime** | `String` | 알림 발송이 종료된 시간 (yyyy-MM-dd HH:mm:ss)     |
| **lastSentUserId** | `Long`     | 마지막으로 알림을 전송한 사용자 ID                 |
| **notifiedUsers**  | `Array`    | 알림을 받은 유저 정보 배열                         |
| **userId**         | `Long`     | 알림을 받은 유저의 ID                             
| **notificationSent** | `Boolean`| 해당 유저에게 알림이 정상적으로 발송되었는지 여부  |

---
## **ERD 구조 및 상세 설명**

재입고 알림 시스템의 ERD(Entity Relationship Diagram)는 아래와 같은 테이블로 구성됩니다.

- **Product**: 상품 정보를 관리하는 테이블
- **ProductUserNotification**: 유저가 특정 상품에 대해 알림을 설정한 정보를 관리하는 테이블
- **ProductUserNotificationHistory**: 유저별로 상품에 대해 알림이 전송된 이력을 관리하는 테이블
- **ProductNotificationHistory**: 상품의 재입고 이력을 관리하는 테이블

```sql
--  Product 테이블 생성
CREATE TABLE Product (
    id BIGINT AUTO_INCREMENT PRIMARY KEY, -- 기본 키 (Primary Key)
    restock_round INT NOT NULL, -- 재입고 라운드 (NOT NULL)
    stock INT NOT NULL, -- 현재 재고 (NOT NULL)
    previousStock INT, -- 이전 재고 (NULL 허용)
    createdAt TIMESTAMP DEFAULT CURRENT_TIMESTAMP, -- 생성 시간
    modifiedAt TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP -- 수정 시간
);
```
```sql
-- ProductUserNotification 테이블 생성 (User-Product 관계 테이블)
CREATE TABLE ProductUserNotification (
    id BIGINT AUTO_INCREMENT PRIMARY KEY, 
    user_id BIGINT NOT NULL, 
    product_id BIGINT NOT NULL, 
    is_active BOOLEAN DEFAULT TRUE, 
    createdAt TIMESTAMP DEFAULT CURRENT_TIMESTAMP, 
    modifiedAt TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP, 
    FOREIGN KEY (product_id) REFERENCES Product(id) ON DELETE CASCADE -- product_id -> id
);
```
```sql
-- ProductUserNotificationHistory 테이블 생성 (알림 전송 이력 테이블)
CREATE TABLE ProductUserNotificationHistory (
    id BIGINT AUTO_INCREMENT PRIMARY KEY, 
    user_id BIGINT NOT NULL, 
    product_id BIGINT NOT NULL, 
    restock_round INT, 
    sent_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, 
    createdAt TIMESTAMP DEFAULT CURRENT_TIMESTAMP, 
    modifiedAt TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP, 
    FOREIGN KEY (product_id) REFERENCES Product(id) ON DELETE CASCADE -- product_id -> id
);
```
```sql
-- ProductNotificationHistory 테이블 생성 (재고 변경 이력 테이블)
CREATE TABLE ProductNotificationHistory (
    id BIGINT AUTO_INCREMENT PRIMARY KEY, 
    product_id BIGINT NOT NULL, 
    last_sent_user_id BIGINT, 
    restock_round INT, 
    status VARCHAR(50), 
    createdAt TIMESTAMP DEFAULT CURRENT_TIMESTAMP, 
    modifiedAt TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP, 
    FOREIGN KEY (product_id) REFERENCES Product(id) ON DELETE CASCADE -- product_id -> id
);
```
## **ERD 관계 설명**
- **Product** (상품) - **ProductUserNotification** (알림 설정)
  - 관계: 1:N 관계 (한 상품에 여러 유저가 알림을 설정할 수 있음)
  - 연결: Product.id → ProductUserNotification.product_id

- **Product** (상품) - **ProductUserNotificationHistory** (알림 발송 이력)
  - 관계: 1:N 관계 (한 상품에 대해 여러 유저에게 알림이 발송됨)
  - 연결: Product.id → ProductUserNotificationHistory.product_id

- **Product** (상품) - **ProductNotificationHistory** (재입고 이력)
  - 관계: 1:N 관계 (하나의 상품은 여러 재입고 이력을 가질 수 있음)
  - 연결: Product.id → ProductNotificationHistory.product_id

- **User** (유저) - **ProductNotificationHistory** (알림 이력)
  - 관계: 1:N 관계 (하나의 유저가 여러 알림을 보낼 수 있음)
  - 연결: User.id → ProductNotificationHistory.last_sent_user_id

## **ERD 다이어그램**
![image](https://github.com/user-attachments/assets/ceacfc4b-979f-45dc-8d66-a207182e9e18)

---
## **동작 흐름도**

다음은 재입고 알림 시스템의 **전체 동작 과정**에 대한 흐름도입니다.

1️⃣ **스케줄러**가 1분마다 재입고 발생 여부를 확인합니다.

2️⃣ 재입고가 발생한 상품에 대해 **알림 전송을 시작**합니다.

3️⃣ ProductNotificationHistory에 알림 상태를 IN_PROGRESS로 저장합니다.

4️⃣ ProductUserNotification 테이블에 등록된 모든 유저에게 알림을 전송합니다.

5️⃣ 재고가 소진되면 알림을 중단하고, 상태를 **CANCELED_BY_SOLD_OUT**로 업데이트합니다.

6️⃣ 모든 유저에게 알림이 성공적으로 발송되면 상태를 **COMPLETED**로 업데이트합니다.

---
## **동작 시연**

### 1. **재입고 알림 전송 API 예시 (Postman)**
<details>
  <summary>🖼️ 재입고 알림 전송 API 보기</summary>
  <br>
  <img src="https://github.com/user-attachments/assets/6488d9f6-c791-47c2-aeff-6512ce3e5f1d" alt="재입고 알림 API" style="max-width:100%;"/>
</details>

### 2. **재입고 알림 전송 API (manual) (Postman)**
<details>
  <summary>🖼️재입고 알림 전송 API (manual) 보기</summary>
  <br>
  <img src="https://github.com/user-attachments/assets/8ea3c6e8-4e4a-4dcd-b4c5-cdc6ca11369e" alt="재입고 알림 전송 API (manual)" style="max-width:100%;"/>
</details>

---
## 📘 **테스트 코드 상세 설명**

### **🧪 RestockNotificationServiceIntegrationTest**

통합 테스트 클래스 **RestockNotificationServiceIntegrationTest**는 데이터베이스와 직접 상호 작용하는 통합 테스트를 포함합니다. 이 테스트는 실제 DB에 접근하여 비즈니스 로직을 검증하는 데 사용됩니다.


### **📋 클래스 정보**
- **테스트 대상**: `RestockNotificationService`
- **사용 기술**: `@SpringBootTest`, `@Transactional`
- **주요 기능**: 재입고 로직, 재입고 알림 발송, 예외 상황을 포함한 전반적인 통합 테스트


### **📌 주요 메서드**

| **메서드명**                      | **설명**                                      |
|---------------------------------|---------------------------------------------|
| **setUp()**                     | 각 테스트 전에 데이터를 초기화하고 상품 및 사용자 알림 데이터를 추가합니다. |
| **findProductById_Success()**    | 상품을 정상적으로 찾는 기능이 올바르게 동작하는지 테스트합니다. |
| **findProductById_Fail_ProductNotFound()** | 존재하지 않는 상품을 조회할 때 예외가 발생하는지 테스트합니다.|
| **createNotificationHistory_Success()** | 알림 히스토리가 정상적으로 생성되는지 테스트합니다. |
| **increaseRestockRound_Success()** | 상품의 재입고 회차가 정상적으로 1 증가하는지 테스트합니다.|
| **updateNotificationStatus_Completed()** | 알림 전송 후 상태가 COMPLETED로 정상 변경되는지 테스트합니다. |
| **sendNotifications_AllUsersNotified()** |모든 활성화된 유저에게 알림이 정상적으로 전송되는지 테스트합니다. |
| **sendNotifications_StopsWhenOutOfStock()** | 알림 전송 중 재고가 소진되었을 때 알림이 중단되는지 테스트합니다 |
| **getLatestNotificationHistory_Success()** | 최신 알림 이력이 정상적으로 조회되는지 테스트합니다.|
| **getLatestNotificationHistory_Fail()** |알림 이력이 없을 때 예외가 발생하는지 테스트합니다. |
| **findActiveUserNotifications_Success()** | 모든 활성화된 유저가 정상적으로 조회되는지 테스트합니다.|

### **소형 테스트 코드 설명**

#### **setUp() - 데이터 초기화**
- 각 테스트 전에 호출되어 데이터를 초기화하고 상품과 사용자 알림 데이터를 추가합니다.
```java
 @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }
```
#### **findProductById_Success()**
- 설명: 상품을 정상적으로 찾는 기능이 올바르게 동작하는지 테스트합니다.
- 검증 사항:
  - 상품 ID로 조회한 결과가 기대한 Product 객체와 일치해야 합니다.
```java
@Test
@DisplayName("성공: 상품을 찾는다.")
void findProductById_Success() {
    // Given
    Long productId = 1L;
    Product product = Product.builder().id(productId).build();
    when(productRepository.findById(productId)).thenReturn(Optional.of(product));

    // When
    Product result = restockNotificationService.findProductById(productId);

    // Then
    assertThat(result).isEqualTo(product);
}
```
#### **findProductById_Fail_ProductNotFound()**
- 설명: 존재하지 않는 상품을 조회할 때 예외가 발생하는지 테스트합니다.
- 검증 사항:
   - 존재하지 않는 상품 ID로 조회했을 때, RestockException 예외가 발생해야 합니다.
```java
@Test
@DisplayName("실패: 존재하지 않는 상품을 조회할 때 예외 발생")
void findProductById_Fail_ProductNotFound() {
    // Given
    Long productId = 1L;
    when(productRepository.findById(productId)).thenReturn(Optional.empty());

    // When & Then
    assertThrows(RestockException.class,
            () -> restockNotificationService.findProductById(1L));
}

```
#### **createNotificationHistory_Success()**
- 설명: 알림 히스토리가 정상적으로 생성되는지 테스트합니다.
- 검증 사항:
  - 알림 히스토리 생성 후, 상태가 IN_PROGRESS여야 합니다
```java
@Test
@DisplayName("성공: 알림 히스토리를 생성한다.")
void createNotificationHistory_Success() {
    // Given
    Product product = Product.builder().id(1L).build();
    ProductNotificationHistory history = ProductNotificationHistory.builder()
            .product(product)
            .status(NotificationStatus.IN_PROGRESS)
            .build();
    when(productNotificationHistoryRepository.save(any(ProductNotificationHistory.class)))
            .thenReturn(history);

    // When
    ProductNotificationHistory result = restockNotificationService.createNotificationHistory(product);

    // Then
    assertThat(result).isNotNull();
    assertThat(result.getStatus()).isEqualTo(NotificationStatus.IN_PROGRESS);
    verify(productNotificationHistoryRepository, times(1)).save(any(ProductNotificationHistory.class));
}

```
#### **increaseRestockRound_Success()**
- 상품의 재입고 회차가 정상적으로 1 증가하는지 테스트합니다.
```java
@Test
@DisplayName("성공: 상품의 재입고 회차가 1 증가한다.")
void increaseRestockRound_Success() {
    // Given
    Product product = Product.builder().id(1L).restockRound(2).build();

    // When
    product.increaseRestockRound();

    // Then
    assertThat(product.getRestockRound()).isEqualTo(3);
}

```
#### **updateNotificationStatus_Completed()**
- 알림 전송 후 상태가 COMPLETED로 정상 변경되는지 테스트합니다.
```java
@Test
@DisplayName("성공: 알림 전송 후 상태가 COMPLETED로 업데이트 된다.")
void updateNotificationStatus_Completed() {
    // Given
    ProductNotificationHistory notificationHistory = ProductNotificationHistory.builder()
            .status(NotificationStatus.IN_PROGRESS)
            .build();

    // When
    restockNotificationService.updateNotificationStatus(notificationHistory, NotificationStatus.COMPLETED);

    // Then
    assertThat(notificationHistory.getStatus()).isEqualTo(NotificationStatus.COMPLETED);
}
```
#### **sendNotifications_AllUsersNotified()**
- 모든 활성화된 유저에게 알림이 정상적으로 전송되는지 테스트합니다.
```java
@Test
@DisplayName("성공: 모든 활성화된 유저에게 알림 전송")
void sendNotifications_AllUsersNotified() {
    // Given
    Product product = Product.builder().id(1L).stock(10).build();
    List<ProductUserNotification> users = Arrays.asList(
            ProductUserNotification.builder().id(1L).userId(1L).build(),
            ProductUserNotification.builder().id(2L).userId(2L).build()
    );
    ProductNotificationHistory notificationHistory = ProductNotificationHistory.builder().build();

    // When
    restockNotificationService.sendNotifications(product, users, notificationHistory);

    // Then
    verify(productUserNotificationHistoryRepository, times(2)).save(any(ProductUserNotificationHistory.class));
}

![image](https://github.com/user-attachments/assets/2fd5e60f-88a5-486b-b51c-7499a1d93b96)

```
#### **sendNotifications_StopsWhenOutOfStock()**
- 알림 전송 중 재고가 소진되었을 때 알림이 중단되는지 테스트합니다.
```java
@Test
@DisplayName("성공: 알림이 중간에 중단된다 (품절 발생)")
void sendNotifications_StopsWhenOutOfStock() {
    // Given
    Product product = Product.builder().id(1L).stock(1).build();
    List<ProductUserNotification> users = Arrays.asList(
            ProductUserNotification.builder().id(1L).userId(1L).build(),
            ProductUserNotification.builder().id(2L).userId(2L).build()
    );
    ProductNotificationHistory notificationHistory = ProductNotificationHistory.builder().build();

    // When
    restockNotificationService.sendNotifications(product, users, notificationHistory);

    // Then
    verify(productUserNotificationHistoryRepository, times(1)).save(any(ProductUserNotificationHistory.class));
}

```
#### **getLatestNotificationHistory_Success()**
- 최신 알림 이력이 정상적으로 조회되는지 테스트합니다.
```java
@Test
@DisplayName("성공: 알림 이력을 가져온다.")
void getLatestNotificationHistory_Success() {
    // Given
    Long productId = 1L;
    ProductNotificationHistory notificationHistory = ProductNotificationHistory.builder().build();
    when(productNotificationHistoryRepository.findTopByProductIdOrderByCreatedAtDesc(productId))
            .thenReturn(Optional.of(notificationHistory));

    // When
    ProductNotificationHistory result = restockNotificationService.getLatestNotificationHistory(productId);

    // Then
    assertThat(result).isNotNull();
}

```
#### **getLatestNotificationHistory_Fail()**
- 알림 이력이 없을 때 예외가 발생하는지 테스트합니다.
```java
@Test
@DisplayName("실패: 알림 이력을 가져오지 못할 때 예외 발생")
void getLatestNotificationHistory_Fail() {
    // Given
    Long productId = 1L;
    when(productNotificationHistoryRepository.findTopByProductIdOrderByCreatedAtDesc(productId))
            .thenReturn(Optional.empty());

    // When & Then
    assertThrows(RestockException.class,
            () -> restockNotificationService.findProductById(1L));
}

```


---
| **메서드명**                      | **설명**                                      |
|---------------------------------|---------------------------------------------|
| **setUp()**                     | 각 테스트 전에 데이터를 초기화하고 상품 및 사용자 알림 데이터를 추가합니다. |
| **restockProduct_Success()**    | 재입고 성공 시 정상적으로 동작하는지 테스트합니다. |
| **restockProduct_StopsWhenOutOfStock()** | 재입고 중 재고 소진 시 알림이 중단되는지 테스트합니다. |
| **retryNotification_Success()** | 알림 재발송이 정상적으로 동작하는지 테스트합니다. |
| **findProductById_Fail_ProductNotFound()** | 존재하지 않는 상품 조회 시 예외가 발생하는지 테스트합니다. |
| **retryNotification_Fail_NoNotificationHistory()** | 알림 이력이 없는 경우 예외가 발생하는지 테스트합니다. |

---

### **📌 통합(중형) 테스트 코드 설명**

#### **setUp() - 데이터 초기화**
- 테스트 실행 전 **@BeforeEach** 어노테이션을 통해 데이터베이스를 초기화합니다.
- 상품, 사용자 알림, 알림 이력과 같은 데이터를 미리 생성하여 각 테스트 메서드에 사용할 수 있도록 준비합니다.
```java
@BeforeEach
void setUp() {
    productUserNotificationHistoryRepository.deleteAll();
    productNotificationHistoryRepository.deleteAll();
    productUserNotificationRepository.deleteAll();
    productRepository.deleteAll();

    product = Product.builder()
            .stock(1) // 재고 1로 설정
            .previousStock(5)
            .restockRound(1)
            .build();
    productRepository.save(product);

    ProductUserNotification user1 = ProductUserNotification.builder()
            .product(product)
            .userId(101L)
            .isActive(true)
            .build();

    ProductUserNotification user2 = ProductUserNotification.builder()
            .product(product)
            .userId(102L)
            .isActive(true)
            .build();

    productUserNotificationRepository.save(user1);
    productUserNotificationRepository.save(user2);
}
```
#### **restockProduct_Success**
-재입고 로직 실행
  - 재입고 서비스 호출 → 상품 재입고 완료, 알림 전송 시작
- 상품의 재입고 회차 증가
  - 상품의 RestockRound가 1 → 2로 정상 증가했는지 확인
- 알림 내역 상태 검증
  - 상품 알림 내역의 상태가 "COMPLETED" 상태로 변경되었는지 확인
- 알림 전송 내역 검증
  - 상품 재입고 후, 알림을 받은 2명의 사용자에 대한 내역이 존재하는지 확인
```java
 @Test
    @DisplayName("통합 테스트: 상품 재입고가 정상적으로 이루어진다.")
    void restockProduct_Success() {
        // When
        RestockResponseDto responseDto = restockNotificationService.restockProduct(product.getId());

        // Then
        Product updatedProduct = productRepository.findById(product.getId()).orElseThrow();
        assertThat(updatedProduct.getRestockRound()).isEqualTo(2); // 🔥 재입고 회차가 1 -> 2 증가

        // 알림 내역 확인
        ProductNotificationHistory notificationHistory = productNotificationHistoryRepository
                .findTopByProductIdOrderByCreatedAtDesc(product.getId())
                .orElseThrow();

        assertThat(notificationHistory.getStatus()).isEqualTo(NotificationStatus.COMPLETED); // 🔥 알림 완료 상태 확인

        // 알림이 두 명에게 전송되었는지 확인
        List<ProductUserNotificationHistory> notificationHistories = productUserNotificationHistoryRepository.findAll();
        assertThat(notificationHistories).hasSize(2);
    }
```
<details>
  <summary>🖼️ restockProduct_Success 실행 화면 보기</summary>
  <br>
  <img src="https://github.com/user-attachments/assets/ed547720-905f-4e8b-b085-42ca1ac51ab8" alt="restockProduct_Success" style="max-width:100%;"/>
</details>

#### **restockProduct_StopsWhenOutOfStock**
- 재고가 1이었기 때문에 1명에게만 알림이 발송.
- 재고가 0이 되자, 알림 내역이 "CANCELED_BY_SOLD_OUT" 상태로 변경.
- 알림이 2명에게 발송되지 않고 1명에게만 전송된 후 중단.

```java
@Test
    @DisplayName("통합 테스트: 재입고 도중 품절이 발생하면 알림이 중단된다.")
    void restockProduct_StopsWhenOutOfStock() {
        // 🔥 상품의 재고를 1로 설정 (두 명에게 알림을 보내다가 중단됨)
        product.addStock(1); // 재고 1로 설정
        productRepository.save(product);

        // When
        RestockResponseDto responseDto = restockNotificationService.restockProduct(product.getId());

        // Then
        Product updatedProduct = productRepository.findById(product.getId()).orElseThrow();
        assertThat(updatedProduct.getStock()).isEqualTo(0); // 🔥 재고가 0이어야 함 (품절)

        // 알림 내역 확인
        ProductNotificationHistory notificationHistory = productNotificationHistoryRepository
                .findTopByProductIdOrderByCreatedAtDesc(product.getId())
                .orElseThrow();

        assertThat(notificationHistory.getStatus()).isEqualTo(NotificationStatus.CANCELED_BY_SOLD_OUT); // 🔥 품절로 인한 중단 상태

        // 알림이 한 명에게만 전송되었는지 확인
        List<ProductUserNotificationHistory> notificationHistories = productUserNotificationHistoryRepository.findAll();
        assertThat(notificationHistories).hasSize(1); // 🔥 알림 1개만 있어야 함
    }
```

<details>
  <summary>🖼️ restockProduct_StopsWhenOutOfStock 실행 화면 보기</summary>
  <br>
  <img src="https://github.com/user-attachments/assets/2b8b07e3-db10-42e4-b173-0df72e81eaef" alt="restockProduct_StopsWhenOutOfStock" style="max-width:100%;"/>
</details>

#### **retryNotification_Successk**
- 품 재입고 후, 특정 조건에 따라 알림을 다시 재발송하는 기능을 검증합니다.
- 중요 포인트: 첫 번째 알림 발송 이후, 이미 알림을 받은 사용자에게는 재발송하지 않고 나머지 사용자에게만 재발송이 이루어지는지를 확인합니다.
- 첫 번째 알림 발송: 2명에게 알림 발송 (알림 2개 생성)
- 알림 재발송: 2명에게 다시 발송 (알림 2개 생성)
- 검증: 알림 내역이 4개인지 확인 (2명 * 2회 = 4개)
- 알림 상태 검증: 알림 내역의 상태가 **"COMPLETED"**로 설정되었는지 확인
```java
@Test
    @DisplayName("통합 테스트: 알림 재발송이 정상적으로 이루어진다.")
    void retryNotification_Success() {
        // 🔥 1차 알림 전송 (2명의 유저에게 전송됨)
        restockNotificationService.restockProduct(product.getId());

        // 🔥 2차 알림 재발송 (첫 번째 유저는 이미 받았으므로 두 번째 유저부터 다시 시작)
        RestockResponseDto responseDto = restockNotificationService.retryNotification(product.getId());

        // Then
        ProductNotificationHistory notificationHistory = productNotificationHistoryRepository
                .findTopByProductIdOrderByCreatedAtDesc(product.getId())
                .orElseThrow();

        assertThat(notificationHistory.getStatus()).isEqualTo(NotificationStatus.COMPLETED); // 🔥 재발송이 완료되었는지 확인

        // 알림 이력 확인 (이미 발송된 유저를 제외하고 재발송이 이루어졌는지 확인)
        List<ProductUserNotificationHistory> notificationHistories = productUserNotificationHistoryRepository.findAll();
        assertThat(notificationHistories).hasSize(4); // 🔥 총 2명 * 2번 발송 = 4개의 알림 기록
    }
```
<details>
  <summary>🖼️ retryNotification_Successk 실행 화면 보기</summary>
  <br>
  <img src="https://github.com/user-attachments/assets/b02ba5d9-cd17-4d5b-a34c-abb2f8c3fe0d" alt="retryNotification_Successk" style="max-width:100%;"/>
</details>

#### **findProductById_Fail_ProductNotFound**
- 존재하지 않는 상품을 조회할 때 예외가 발생하는지 검증합니다.
- 일부로 IllegalArgumentException를 호출시켜 로그 확인
```java
@Test
    @DisplayName("통합 테스트: 상품 조회 후 예외가 발생한다.")
    void findProductById_Fail_ProductNotFound() {
        // When & Then
        assertThrows(IllegalArgumentException.class,
                () -> restockNotificationService.findProductById(999L));
    }
```

<details>
  <summary>🖼️ findProductById_Fail_ProductNotFound 실행 화면 보기</summary>
  <br>
  <img src="https://github.com/user-attachments/assets/e29c98ea-aaa4-4832-9d52-55b1ac3ac8c5" alt="findProductById_Fail_ProductNotFound" style="max-width:100%;"/>
</details>

#### **retryNotification_Fail_NoNotificationHistory**
- 재입고 이력이 없는 상태에서 알림 재발송을 시도했을 때 예외가 발생하는지 검증합니다.
- 일부로 IllegalArgumentException를 호출시켜 로그 확인
```java
 @Test
    @DisplayName("통합 테스트: 재입고 이력이 없을 때 예외가 발생한다.")
    void retryNotification_Fail_NoNotificationHistory() {
        // When & Then
        assertThrows(IllegalArgumentException.class,
                () -> restockNotificationService.retryNotification(999L));
    }
```
<details>
  <summary>🖼️ retryNotification_Fail_NoNotificationHistory 실행 화면 보기</summary>
  <br>
  <img src="https://github.com/user-attachments/assets/e943b741-f839-42c6-a46f-403fd546450c" alt="retryNotification_Fail_NoNotificationHistory" style="max-width:100%;"/>
</details>

