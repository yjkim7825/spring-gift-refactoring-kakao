package gift.acceptance;

import com.jayway.jsonpath.JsonPath;
import io.cucumber.java.en.Given;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.ResponseEntity;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class LoginSteps {
    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private LoggedInMember loggedInMember;

    @Given("{string} 회원이 {string} 비밀번호로 로그인해 있다")
    public void 회원이_로그인해_있다(String email, String password) {
        ResponseEntity<String> response = restTemplate.postForEntity(
            "/api/members/login",
            Map.of("email", email, "password", password),
            String.class
        );
        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        loggedInMember.loggedInWith(JsonPath.read(response.getBody(), "$.token"));
    }
}
