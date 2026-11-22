package gift.acceptance;

import com.jayway.jsonpath.JsonPath;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class MemberSteps {
    @Autowired
    private TestRestTemplate restTemplate;

    private ResponseEntity<String> response;

    @When("{string} 이메일과 {string} 비밀번호로 회원가입한다")
    public void 회원가입한다(String email, String password) {
        response = restTemplate.postForEntity(
            "/api/members/register",
            Map.of("email", email, "password", password),
            String.class
        );
    }

    @Then("가입이 완료된다")
    public void 가입이_완료된다() {
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    }

    @Then("따로 로그인하지 않고 비어 있는 내 위시를 볼 수 있다")
    public void 따로_로그인하지_않고_비어_있는_내_위시를_볼_수_있다() {
        String token = JsonPath.read(response.getBody(), "$.token");
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);

        ResponseEntity<String> wishes = restTemplate.exchange(
            "/api/wishes",
            HttpMethod.GET,
            new HttpEntity<>(headers),
            String.class
        );

        assertThat(wishes.getStatusCode()).isEqualTo(HttpStatus.OK);
        List<Object> content = JsonPath.read(wishes.getBody(), "$.content");
        assertThat(content).isEmpty();
    }

    @Then("{string} 안내와 함께 가입이 거절된다")
    public void 안내와_함께_가입이_거절된다(String message) {
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isEqualTo(message);
    }

    @Then("가입이 거절된다")
    public void 가입이_거절된다() {
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }
}
