# 프로젝트 온보딩 가이드

## 개요

이 프로젝트는 커머스 백엔드 서버로, Kotlin과 Spring Boot 3 기반의 REST API 서버입니다. DDD나 헥사고날 아키텍처를 적용하지 않으며, 현재 규모에 맞는 실용적인 계층형 구조를 채택하고 있습니다. 빌드 도구는 Gradle(Kotlin DSL)을 사용하고, Java 21 기반입니다.

---

## 모듈 구조

프로젝트는 Gradle 멀티모듈로 구성되어 있으며, 크게 세 가지 루트 디렉토리로 나뉩니다.

```
dev-practice-commerce/
├── core/
│   ├── core-enum      # 프로젝트 전체에서 공유하는 Enum 모음
│   └── core-api       # 실행 가능한 메인 서버 모듈 (bootJar 활성화)
├── storage/
│   └── db-core        # JPA Entity + Repository 모음 (MySQL / H2)
└── support/
    ├── logging        # Logback 설정 및 로깅 관련 yml
    └── monitoring     # Prometheus / Actuator 설정
```

의존 방향은 단방향입니다.

```
core-api  ──→  core-enum
core-api  ──→  storage:db-core
core-api  ──→  support:logging
core-api  ──→  support:monitoring
db-core   ──→  core-enum
```

`core-api`가 유일하게 실행 가능한 모듈입니다. 나머지는 모두 일반 JAR로 빌드되어 `core-api`에 의존성으로 포함됩니다.

`core-enum`은 `OrderState`, `PaymentState`, `CouponType` 등 여러 모듈이 공통으로 사용하는 Enum만 모아놓은 모듈입니다. `db-core`와 `core-api`가 동시에 참조하기 때문에 별도 모듈로 분리되어 있습니다.

`storage:db-core`는 영속성 계층만 담당합니다. JPA Entity, Spring Data Repository, DataSource 설정, AES 암호화 컨버터가 이 모듈에 있습니다. `core-api`는 이 모듈의 Entity와 Repository를 직접 참조합니다.

`support/logging`과 `support/monitoring`은 yml 설정 파일과 의존성만 제공하는 얇은 모듈입니다. `core-api`의 `application.yml`이 이 설정들을 `spring.config.import`로 가져옵니다.

---

## 패키지 구조

`core-api` 내부 패키지는 역할별로 명확히 나뉩니다.
dlwjs
```
io.dodn.commerce/
├── CoreApiApplication.kt
├── core/
│   ├── api/
│   │   ├── config/          # 비동기 설정(AsyncConfig), WebMvc 설정(WebConfig)
│   │   └── controller/
│   │       ├── ApiControllerAdvice.kt   # 전역 예외 처리
│   │       ├── v1/          # 일반 API 컨트롤러
│   │       │   ├── request/ # Request DTO
│   │       │   └── response/# Response DTO
│   │       └── batch/       # 배치 트리거용 API (추후 분리 예정)
│   ├── domain/              # 개념 객체 + 서비스 + 로직 클래스
│   └── support/
│       ├── auth/            # UserArgumentResolver
│       ├── error/           # CoreException, ErrorType, ErrorCode
│       └── response/        # ApiResponse, PageResponse
```

`storage:db-core`의 패키지 구조는 단순합니다.

```
io.dodn.commerce.storage.db.core/
├── *Entity.kt          # JPA Entity
├── *Repository.kt      # Spring Data JPA Repository
├── BaseEntity.kt       # 공통 베이스 Entity (id, status, createdAt, updatedAt)
├── config/             # DataSource, JPA 설정
└── converter/          # AES 암호화 컨버터
```

`core.domain` 패키지에는 서비스, 로직 클래스, 개념 객체가 모두 함께 위치합니다. 현재 클래스 수가 많지 않아 하위 패키지로 나누지 않고 있으며, 클래스가 지나치게 많아지는 시점에 개념별 응집도를 유지하며 패키지를 나눌 수 있습니다.

---

## 설계 스타일 — 4계층 아키텍처

이 프로젝트는 논리적으로 4개의 계층을 갖습니다. 물리적으로 별도 패키지로 분리되어 있지는 않지만, 클래스 명명과 역할로 구분됩니다.

```
Presentation Layer   (Controller)
       ↓
Business Layer       (*Service)
       ↓
Logic Layer          (*Finder, *Reader, *Handler, *Manager, ...)
       ↓
Data Access Layer    (*Repository, *Client)
```

계층 참조는 반드시 위에서 아래로만 흐릅니다. 역방향 참조, 계층 건너뜀은 허용되지 않습니다. 단, Logic Layer 클래스들끼리는 서로 참조할 수 있습니다. 예를 들어 `ReviewManager`가 조회를 위해 `ReviewFinder`를 사용하는 것은 허용됩니다.

### Presentation Layer (Controller)

컨트롤러는 요청을 받아 개념 객체로 변환한 후 Service에 전달하는 역할만 합니다. 비즈니스 로직을 담지 않습니다. Request DTO는 `.toXxx()` 메서드로 개념 객체를 만들며, 이 과정에서 길이 검증이나 빈 값 검증 같은 입력 유효성 검사를 함께 처리합니다.

```kotlin
// AddReviewRequest.toContent() 예시
fun toContent(): ReviewContent {
    if (rate <= BigDecimal.ZERO) throw CoreException(ErrorType.INVALID_REQUEST)
    if (content.isEmpty()) throw CoreException(ErrorType.INVALID_REQUEST)
    return ReviewContent(rate, content)
}
```

여러 Service를 조합해야 하는 경우 `Assembler` 클래스를 `io.dodn.commerce.core.api.assembler` 패키지에 두고 컨트롤러가 이를 호출합니다. Assembler는 `*Service`만 참조 가능하고, Logic Layer나 Repository를 직접 참조할 수 없습니다.

### Business Layer (Service)

`@Service`로 선언하며 `@Transactional`을 사용하지 않습니다. Logic Layer 컴포넌트들을 조합해 비즈니스 흐름이 한눈에 보이도록 구현하는 것이 목표입니다.

```kotlin
// ReviewService — 비즈니스 흐름이 읽히도록 구현
fun addReview(user: User, target: ReviewTarget, content: ReviewContent): Long {
    val reviewKey = reviewPolicyValidator.validateNew(user, target)  // 정책 검증
    val reviewId = reviewManager.add(reviewKey, target, content)     // 저장
    pointHandler.earn(user, PointType.REVIEW, reviewId, PointAmount.REVIEW) // 포인트 적립
    return reviewId
}
```

### Logic Layer (Finder, Manager, Handler, ...)

`@Component`로 선언하며, 실제 비즈니스 처리와 데이터 접근이 일어나는 계층입니다. `@Transactional`은 이 계층에서만 사용합니다. Repository에서 가져온 Entity를 직접 반환하지 않고, 개념 객체로 변환해서 반환합니다.

클래스 명명 규칙이 역할을 명시합니다.
- `*Finder` — 조회 전담. 여러 Repository를 조합해 개념 객체를 만들어 반환
- `*Manager` — 상태 변경이 필요한 쓰기 작업
- `*Handler` — 여러 개념에 걸친 횡단 관심사 처리 (예: `PointHandler`)
- `*Calculator`, `*Validator` 등 — 계산, 검증 같은 특화 역할

Logic Layer끼리 타 개념의 데이터가 필요할 때는 Repository에 직접 의존하기보다 해당 개념의 Logic 클래스를 우선 사용합니다.

### Data Access Layer

Spring Data JPA Repository만 있습니다. 기존 메서드 시그니처 변경은 지양하고, 필요한 쿼리는 새 메서드를 추가해서 대응합니다.

---

## 코드 스타일

### "개념"이라는 용어 사용

이 프로젝트는 DDD를 적용하지 않아, "도메인 객체"라는 표현 대신 "개념 객체"라는 표현을 사용합니다. `core.domain` 패키지 안의 순수 Kotlin 클래스들이 개념 객체입니다. JPA Entity와 분리되어 있으며, Logic Layer가 Entity를 개념 객체로 변환하는 책임을 집니다.

### 에러 처리

모든 비즈니스 예외는 `CoreException(ErrorType)`으로 던집니다. `ErrorType`은 HTTP 상태코드, 에러코드, 메시지, 로그 레벨을 하나의 Enum 값에 담고 있어, 새 에러 케이스 추가 시 여기에만 항목을 추가하면 됩니다. `ApiControllerAdvice`가 이를 catch해 `ApiResponse`로 직렬화합니다.

### API 응답 포맷

모든 응답은 `ApiResponse<T>` 형식입니다.
```json
{ "result": "SUCCESS", "data": { ... }, "error": null }
```
응답 데이터가 없는 경우 `ApiResponse<Any>`를 사용합니다. Response DTO에 변환 로직이 필요한 경우 `*Response.of(...)` 정적 팩토리 메서드를 사용합니다.

### 인증

이 서버는 API Gateway 뒤에 위치한다고 가정합니다. 인증은 Gateway에서 처리되고, `DODN-Commerce-User-Id` 헤더로 userId만 넘어옵니다. `UserArgumentResolver`가 이 헤더를 `User` 개념 객체로 변환하며, 컨트롤러 메서드 파라미터에 `User`를 선언하면 자동으로 주입됩니다.

### Soft Delete

물리 삭제 없이 `EntityStatus`로 상태를 관리합니다. 모든 Entity는 `BaseEntity`를 상속하고 있으며 `delete()`, `active()`, `isActive()`, `isDeleted()` 메서드를 제공합니다. 조회 시 삭제된 데이터는 기본적으로 필터링하며, 재활성화는 Manager가 비즈니스 규칙을 검증한 뒤 `active()`를 호출합니다.

### 상수 관리

하드코딩이나 매직 넘버를 지양하고 Enum 또는 Object로 추출합니다. 예를 들어 포인트 적립액은 `PointAmount` 오브젝트에, 정산 수수료율은 `SettlementCalculator` 오브젝트에 명시적으로 선언되어 있습니다.

### 테스트

`ContextTest`를 상속받아 `@TestConstructor(autowireMode = ALL)`로 생성자 주입을 받습니다. 로컬 테스트는 H2 인메모리 DB(`local` 프로파일)를 사용합니다. 테스트 태그는 세 가지입니다. `context`는 Spring 컨텍스트를 띄우는 통합 테스트, `unitTest`는 컨텍스트 없이 빠르게 실행되는 단위 테스트, `develop`은 실제 DB나 외부 연동이 필요한 개발용 테스트로, 일반 `test` 태스크에서는 실행되지 않습니다.