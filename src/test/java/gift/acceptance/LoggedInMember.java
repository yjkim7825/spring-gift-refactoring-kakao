package gift.acceptance;

import io.cucumber.spring.ScenarioScope;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;

/**
 * 시나리오 안에서 로그인한 회원. 여러 스텝 클래스가 같은 회원으로 요청할 때 쓴다.
 */
@Component
@ScenarioScope
public class LoggedInMember {
    private String token;

    public void loggedInWith(String token) {
        this.token = token;
    }

    public <T> HttpEntity<T> request(T body) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        return new HttpEntity<>(body, headers);
    }
}
