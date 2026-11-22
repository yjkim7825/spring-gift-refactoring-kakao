# 2단계. 코드 모양 맞추기 작업 목록

작업 전에 작성함. [리팩터링 계획](../README.md)의 2단계를 커밋 하나 크기로 나눴다.

## 작업 목록

모든 항목은 컴파일하면 같은 프로그램이 되거나, 같은 값을 다른 이름으로 쓰는 변경이다. 회원, 손님, 관리자가 겪는 동작은 바뀌지 않는다.

| 순서 | #   | 할 일                                                                                        | 대상               | 도구                | 상태  |
| ---- | --- | -------------------------------------------------------------------------------------------- | ------------------ | ------------------- | ----- |
| 1    | S1  | 부품을 연결하는 표시 중 필요 없는 것을 뺀다                                                  | 4곳                | 손으로, 검사로 확인 | 할 일 |
| 2    | S3  | 주소를 적는 방식을 짧은 쪽으로 맞춘다                                                        | 5곳                | 일괄 치환           | 할 일 |
| 3    | S4  | 목록을 만드는 방식을 나머지와 같게 맞춘다                                                    | 1곳                | 손으로              | 할 일 |
| 4    | S5  | 응답 결과를 숫자 대신 이름으로 적는다                                                        | 6곳                | 일괄 치환           | 할 일 |
| 5    | S6  | 주문의 응답 종류를 다른 기능처럼 적는다                                                      | 2곳                | 손으로              | 할 일 |
| 6    | S8  | 로그인 정보와 보내는 내용의 형식을 알리는 이름을 미리 정의된 이름으로 쓴다                   | 9곳                | 일괄 치환           | 할 일 |
| 7    | S2  | 변수의 종류를 직접 적는 방식으로 맞춘다                                                      | 30곳               | IDE 검사            | 할 일 |
| 8    | S7  | 파일 앞 설명 주석에서 작성자 표시와 이름만 되풀이하는 설명을 빼고, 규칙 설명은 형식을 맞춘다 | 11개 파일          | 손으로              | 할 일 |
| 9    | S10 | 이번에 정한 모양 중 검사할 수 있는 것을 검사 도구로 고정한다                                 | 규칙은 그때 정한다 | Checkstyle          | 할 일 |

S9(상품명과 옵션명 검사를 부르는 코드의 중복)는 3단계에서 처리한다.

순서는 바꾸는 줄이 적은 것부터 잡았다. S2는 주문과 위시의 같은 줄을 S5, S6, S8과 함께 건드리므로 뒤로 미뤘다. 그래야 각 커밋의 변경 내용에 그 항목만 보인다.

## 커밋마다 확인할 것

1. 그 항목의 검사 명령이 아무것도 출력하지 않는다.
2. 인수 테스트 62개가 모두 통과한다.
3. 바뀐 파일이 그 항목의 대상 파일뿐이다.
4. 인수 테스트와 문서는 고치지 않는다. 동작이 바뀌지 않으므로 사용자 스토리와 용어 사전도 그대로다.

## 정한 것

1. S7은 규칙을 설명하지 않고 "회원을 나타낸다"처럼 이름을 되풀이할 뿐인 설명을 지운다. 작성자 표시도 지운다. 옵션 규칙, 로그인 흐름, 옵션명 규칙을 설명하는 3개 파일의 주석은 남기고 형식만 맞춘다.
2. S8에 "Content-Type"(보내는 내용의 형식을 알리는 이름) 2곳도 넣는다.
3. 로그인 정보 앞에 붙는 "Bearer " 글자는 그대로 둔다. 작동 변경 후보 C2-1과 함께 4단계에서 본다.
4. 2단계 마지막(S10)에 Checkstyle을 넣는다. 이번에 정한 모양 중 검사할 수 있는 것만 넣고, 어떤 규칙을 넣을지는 그때 목록을 보고 정한다.

## 작동 변경 후보

고치지 않고 적어만 둔다.

| #    | 발견한 것                                                                                  | 관련               |
| ---- | ------------------------------------------------------------------------------------------ | ------------------ |
| C2-1 | 로그인 정보에서 "Bearer " 글자를 떼어 낼 때 앞에 붙었는지 확인하지 않고 어디에 있든 지운다 | S8. 4단계에서 본다 |

---

## 개발자 참고

본문을 코드와 연결하기 위한 정보다. 비개발자는 읽지 않아도 된다.

### 도구에 대해

- 이 프로젝트에는 Java 포매터나 린터가 설정되어 있지 않다. `org.jlleitschuh.gradle.ktlint`는 Kotlin 전용이고, Kotlin 소스는 없다.
- Spotless(google-java-format 등)를 돌리면 S1~S8과 관계없는 줄까지 모든 파일의 서식이 바뀐다. 빌드 파일 변경도 요청 범위 밖이라 이번에는 쓰지 않는다.
- 아래 "도구" 칸의 IntelliJ 검사 이름과 OpenRewrite 레시피는 실행해서 확인하지 않았다(확인 필요). 쓸 수 없으면 손으로 고치고 검사 명령으로 확인한다.
- 검사 명령은 린터 대신 쓰는 `grep`이다. 고친 뒤 아무것도 출력하지 않아야 한다.

### 1. S1 `@Autowired` 제거

- 커밋 메시지: `refactor: remove redundant @Autowired on single constructors`
- 대상: `AuthenticationResolver.java:19`, `JwtProvider.java:23`, `AdminMemberController.java:23`, `MemberController.java:27`. 쓰지 않게 되는 `import org.springframework.beans.factory.annotation.Autowired;`도 지운다.
- 동작: 생성자가 하나면 Spring이 `@Autowired` 없이도 생성자 주입을 한다.
- 도구: OpenRewrite `org.openrewrite.java.spring.NoAutowiredOnConstructor`(rewrite-spring, 확인 필요). 4곳이라 손으로 고쳐도 된다.
- 검사: `grep -rn "@Autowired\|annotation.Autowired" src/main/java`

### 2. S3 `path =` 제거

- 커밋 메시지: `refactor: drop path attribute name in request mappings`
- 대상: `KakaoAuthController.java:21, 40, 55`, `OptionController.java:25, 69`
- 동작: `path`와 `value`는 서로의 별칭이라 같다.
- 도구: `sed -i -E 's/(Mapping)\(path = /\1(/' src/main/java/gift/auth/KakaoAuthController.java src/main/java/gift/option/OptionController.java`
- 검사: `grep -rn "Mapping(path = " src/main/java`

### 3. S4 `Collectors.toList()` 교체

- 커밋 메시지: `refactor: use Stream.toList in OptionController`
- 대상: `OptionController.java:43`. 쓰지 않게 되는 `import java.util.stream.Collectors;`(18행)도 지운다.
- 동작: `Stream.toList()`는 수정할 수 없는 목록을 돌려준다. 이 목록은 응답으로 직렬화만 하고 수정하지 않으므로 응답이 같다.
- 도구: IntelliJ 검사 "Stream API call chain can be simplified"(확인 필요). 1곳이라 손으로 고친다.
- 검사: `grep -rn "Collectors" src/main/java`

### 4. S5 상태 코드 숫자를 `HttpStatus`로

- 커밋 메시지: `refactor: use HttpStatus constants instead of numeric status codes`
- 대상: `OrderController.java:55, 77`, `WishController.java:45, 59, 87`(`401`), `WishController.java:96`(`403`). 두 파일에 `import org.springframework.http.HttpStatus;`를 더한다.
- 동작: `HttpStatus.UNAUTHORIZED`는 401, `HttpStatus.FORBIDDEN`은 403이다.
- 도구: `sed -i 's/status(401)/status(HttpStatus.UNAUTHORIZED)/; s/status(403)/status(HttpStatus.FORBIDDEN)/' src/main/java/gift/order/OrderController.java src/main/java/gift/wish/WishController.java`
- 검사: `grep -rnE "status\([0-9]+\)" src/main/java`

### 5. S6 `ResponseEntity<?>`에 타입 적기

- 커밋 메시지: `refactor: declare response types in OrderController`
- 대상: `OrderController.java:48` `getOrders`는 `ResponseEntity<Page<OrderResponse>>`, `:70` `createOrder`는 `ResponseEntity<OrderResponse>`. `import org.springframework.data.domain.Page;`를 더한다.
- 동작: 제네릭은 컴파일 때만 쓰인다. `status(...).build()`와 `notFound().build()`는 타입 추론으로 그대로 컴파일된다. Jackson은 실제 객체 타입으로 직렬화하므로 응답이 같다.
- 도구: 손으로 고친다.
- 검사: `grep -rn "ResponseEntity<?>" src/main/java`

### 6. S8 헤더 이름과 값을 Spring 상수로

- 커밋 메시지: `refactor: use HttpHeaders and MediaType constants for headers`
- 대상 9곳:
  - `@RequestHeader("Authorization")` 5곳: `OrderController.java:49, 71`, `WishController.java:39, 53, 81`
  - `.header("Authorization", ...)` 2곳: `KakaoLoginClient.java:38`, `KakaoMessageClient.java:24`
  - `.header("Content-Type", "application/x-www-form-urlencoded")` 2곳: `KakaoLoginClient.java:29`, `KakaoMessageClient.java:25`
- 바꿀 값: `"Authorization"`은 `HttpHeaders.AUTHORIZATION`, `"Content-Type"`은 `HttpHeaders.CONTENT_TYPE`, `"application/x-www-form-urlencoded"`는 `MediaType.APPLICATION_FORM_URLENCODED_VALUE`
- 더할 import: 4개 파일에 `org.springframework.http.HttpHeaders`, 두 Kakao 클라이언트에 `org.springframework.http.MediaType`
- 동작: 세 상수 모두 같은 문자열의 컴파일 상수다. 어노테이션 값으로도 쓸 수 있다. 보내는 요청과 받는 요청이 같다.
- 범위 밖: `"Bearer "` 3곳(`AuthenticationResolver.java:27`, `KakaoLoginClient.java:38`, `KakaoMessageClient.java:24`)은 Spring에 상수가 없어 그대로 둔다. C2-1과 함께 4단계에서 본다.
- 도구: 아래 `sed`를 쓰고, import는 손으로 더한다. 커밋 전에 `git diff`를 확인받는다.

```
sed -i 's/"Authorization"/HttpHeaders.AUTHORIZATION/' src/main/java/gift/order/OrderController.java src/main/java/gift/wish/WishController.java src/main/java/gift/auth/KakaoLoginClient.java src/main/java/gift/order/KakaoMessageClient.java
sed -i 's/"Content-Type", "application\/x-www-form-urlencoded"/HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED_VALUE/' src/main/java/gift/auth/KakaoLoginClient.java src/main/java/gift/order/KakaoMessageClient.java
```

- 검사: `grep -rnE '"(Authorization|Content-Type|application/x-www-form-urlencoded)"' src/main/java`
- 작동 변경 후보 C2-1: `AuthenticationResolver.extractMember`는 `authorization.replace("Bearer ", "")`로 접두사가 아닌 위치의 글자도 지운다. 고치지 않는다.

### 7. S2 `var`와 지역 변수 `final`을 명시 타입으로

- 커밋 메시지: `refactor: use explicit types for local variables`
- 대상: `var` 19곳, 지역 변수 `final` 11곳. 필드의 `final`은 그대로 둔다.

| 파일                          | `var` | 지역 `final` | 바꿀 타입                                                            |
| ----------------------------- | ----- | ------------ | -------------------------------------------------------------------- |
| `OrderController.java`        | 7     | 0            | `Member`, `Page<OrderResponse>`, `Option`, `int`, `Order`, `Product` |
| `WishController.java`         | 8     | 0            | `Member`, `Page<WishResponse>`, `Product`, `Wish`                    |
| `KakaoMessageClient.java`     | 4     | 0            | `String`, `LinkedMultiValueMap<String, String>`                      |
| `AuthenticationResolver.java` | 0     | 2            | `String`                                                             |
| `JwtProvider.java`            | 0     | 2            | `Date`                                                               |
| `MemberController.java`       | 0     | 4            | `Member`, `String`                                                   |
| `AdminMemberController.java`  | 0     | 3            | `Member`                                                             |

- 더할 import: `OrderController`에 `gift.product.Product`, `org.springframework.data.domain.Page`(S6에서 이미 더했으면 생략), `WishController`에 `gift.member.Member`, `gift.product.Product`, `org.springframework.data.domain.Page`(이미 있음), `gift.wish.Wish`는 같은 패키지라 필요 없다.
- 동작: `var`는 컴파일 때 같은 타입으로 바뀌므로 바이트코드가 같다. 새 import는 소스 표기만 늘린다. 클래스 사이 의존은 바이트코드에 이미 있으므로 4단계 ArchUnit 결과도 같다.
- 타입은 오른쪽 식의 타입을 그대로 쓴다. 예를 들어 `new LinkedMultiValueMap<String, String>()`을 `MultiValueMap`으로 넓히지 않는다.
- 도구: IntelliJ 의도 동작 "Replace 'var' with explicit type"과 검사 "Unnecessary 'final' on local variable or parameter"(둘 다 확인 필요). 쓸 수 없으면 손으로 고친다.
- 검사: `grep -rnE "^\s+var " src/main/java`, `grep -rnE "^\s{8,}final \w" src/main/java`

### 8. S7 클래스 설명 주석

- 커밋 메시지: `refactor: keep only rule-describing class comments as Javadoc`

| 파일                          | 지금    | 지금 설명                                                       | 바꿀 것                                        |
| ----------------------------- | ------- | --------------------------------------------------------------- | ---------------------------------------------- |
| `AuthenticationResolver.java` | Javadoc | Resolves the authenticated member from an Authorization header. | 클래스 주석 전체 삭제                          |
| `JwtProvider.java`            | Javadoc | Provides JWT token creation and validation.                     | 클래스 주석 전체 삭제. 메서드 Javadoc은 그대로 |
| `TokenResponse.java`          | Javadoc | Response containing a JWT access token.                         | 클래스 주석 전체 삭제                          |
| `AdminMemberController.java`  | Javadoc | Admin controller for managing members.                          | 클래스 주석 전체 삭제                          |
| `Member.java`                 | Javadoc | Represents a registered member.                                 | 클래스 주석 전체 삭제                          |
| `MemberController.java`       | Javadoc | Handles member registration and login.                          | 클래스 주석 전체 삭제                          |
| `MemberRepository.java`       | Javadoc | Repository for Member entities.                                 | 클래스 주석 전체 삭제                          |
| `MemberRequest.java`          | Javadoc | Request body for member registration and login.                 | 클래스 주석 전체 삭제                          |
| `KakaoAuthController.java`    | `/* */` | 카카오 로그인 흐름 2단계                                        | Javadoc으로 바꾼다. 내용은 그대로              |
| `OptionController.java`       | `/* */` | 상품마다 옵션이 1개 이상 있어야 한다는 규칙, 옵션명 검증        | Javadoc으로 바꾼다. 내용은 그대로              |
| `OptionNameValidator.java`    | `/* */` | 옵션명 규칙 3가지                                               | Javadoc으로 바꾼다. 내용은 그대로              |

- 지우는 8개는 모두 클래스 이름을 문장으로 되풀이할 뿐 규칙을 설명하지 않는다. `@author brian.kim`, `@since 1.0`도 함께 지워진다.
- `OptionController`의 "Each product must have at least one option at all times."는 [안건 #3](../../agenda.md#고칠지-정할-동작)과 모순 M-1의 근거다. 문장을 바꾸거나 지우지 않는다.
- Javadoc으로 바꾸면 `KakaoAuthController`, `OptionNameValidator`의 목록 줄이 Javadoc 도구에서 한 문단으로 이어 보일 수 있다. 소스에서 읽는 모양은 같고 동작에는 영향이 없어 그대로 둔다.
- 주석이 없는 클래스에 새로 달지 않는다.
- 도구: 손으로 고친다.
- 검사: `grep -rn "@author\|@since" src/main/java`, `grep -rnE "^/\*([^*]|$)" src/main/java`

### 9. S10 Checkstyle로 모양 고정

- 커밋 메시지: 넣을 규칙을 정한 뒤 정한다.
- 할 일: `build.gradle.kts`에 Gradle `checkstyle` 플러그인과 설정 파일을 더하고, S1부터 S8까지 정한 모양 중 Checkstyle로 검사할 수 있는 것만 규칙으로 넣는다.
- 진행: 시작할 때 S1부터 S8 각각을 Checkstyle로 검사할 수 있는지와 해당 검사 모듈을 목록으로 보여 주고, 넣을 규칙을 물어서 정한다.
- 확인: 2단계를 마친 코드에서 `gradlew checkstyleMain`이 통과하고, 규칙을 일부러 어긴 코드에서는 실패한다.
- 빌드 파일을 바꾸는 항목이라, 다른 항목처럼 커밋 하나로 따로 둔다.

### 테스트

- 커밋마다 `gradlew test --rerun`을 실행한다. `gradlew test`는 `UP-TO-DATE`로 건너뛸 수 있다.
- `build/test-results/test/*.xml`에서 `tests="62" failures="0" errors="0" skipped="0"`을 확인한다.
