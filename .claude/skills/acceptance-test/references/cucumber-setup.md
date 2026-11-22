# Cucumber 환경 세우기

## 의존성

추가하기 전에 사용자에게 보여 준다.

```kotlin
testImplementation(platform("io.cucumber:cucumber-bom:7.22.2"))
testImplementation("io.cucumber:cucumber-java")
testImplementation("io.cucumber:cucumber-spring")
testImplementation("io.cucumber:cucumber-junit-platform-engine")
testImplementation("org.junit.platform:junit-platform-suite")
```

| 줄                               | 빼면 생기는 일                                        |
| -------------------------------- | ----------------------------------------------------- |
| `cucumber-bom`                   | 버전을 줄마다 적어야 한다                             |
| `cucumber-java`                  | 스텝 어노테이션이 없다                                |
| `cucumber-spring`                | 스텝에서 서버나 빈을 주입받을 수 없다                 |
| `cucumber-junit-platform-engine` | Gradle로 Cucumber를 실행할 수 없다                    |
| `junit-platform-suite`           | Gradle은 클래스만 훑으므로 feature 파일을 찾지 못한다 |

HTTP 호출은 `spring-boot-starter-test`의 `TestRestTemplate`으로 충분하다.

버전은 프로젝트의 JUnit Platform과 맞춘다. Spring Boot 3.5는 1.12를 쓰고, Cucumber 7.24부터는 1.13, 8.x는 JUnit 6이 필요하다. Boot 버전이 바뀌면 `cucumber-junit-platform-engine`의 pom에 있는 `junit-jupiter.version`을 보고 다시 고른다.

## AcceptanceTest

`src/test/java/gift/acceptance/AcceptanceTest.java`

```java
package gift.acceptance;

import io.cucumber.spring.CucumberContextConfiguration;
import org.junit.platform.suite.api.IncludeEngines;
import org.junit.platform.suite.api.SelectClasspathResource;
import org.junit.platform.suite.api.Suite;
import org.springframework.boot.test.context.SpringBootTest;

@Suite
@IncludeEngines("cucumber")
@SelectClasspathResource("features")
@CucumberContextConfiguration
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class AcceptanceTest {
}
```

- 한 클래스가 실행 시작점(`@Suite`)과 서버 설정(`@CucumberContextConfiguration`)을 함께 맡는다. 따로 등록할 곳은 없다. Gradle이 넘긴 클래스 중 Suite 엔진이 `@Suite`가 붙은 것을 고른다.
- glue는 지정하지 않는다. 지정하지 않으면 클래스패스 전체에서 스텝을 찾는다.
- `abstract`로 만들지 않는다. 실행 대상에서 빠져서 0건으로 통과한다.
- feature 파일은 `src/test/resources/features/`에 둔다.

## 첫 스텝 정의 예시

따를 스텝이 아직 없을 때의 본보기다. 실제 예는 US-04의 `CategorySteps`다.

```java
public class CategorySteps {
    @Autowired
    private TestRestTemplate restTemplate;

    private ResponseEntity<List<Map<String, Object>>> response;

    @When("손님이 카테고리 목록을 본다")
    public void 손님이_카테고리_목록을_본다() {
        response = restTemplate.exchange("/api/categories", HttpMethod.GET, null,
            new ParameterizedTypeReference<>() {
            });
    }

    @Then("다음 카테고리가 빠짐없이 보인다")
    public void 다음_카테고리가_빠짐없이_보인다(List<String> names) {
        assertThat(response.getBody())
            .extracting(category -> category.get("name"))
            .containsExactlyInAnyOrderElementsOf(names);
    }
}
```

- 어노테이션은 `io.cucumber.java.en`의 것을 쓴다. feature의 한글 키워드와 상관없이 문장으로 연결된다.
- 응답은 JSON 필드로 읽는다. 프로덕션 응답 클래스로 받지 않는다.
- 스텝 클래스는 시나리오마다 새로 만들어지므로 응답을 필드에 둬도 된다. 여러 스텝 클래스가 응답을 나눠 써야 할 때만 공유 객체를 만든다.
