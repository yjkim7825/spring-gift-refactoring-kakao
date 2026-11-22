package gift.acceptance;

import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.http.client.ClientHttpRequestFactorySettings;
import org.springframework.boot.http.client.ClientHttpRequestFactorySettings.Redirects;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;

public class KakaoLoginSteps {
    @Autowired
    private TestRestTemplate restTemplate;

    private ResponseEntity<Void> response;

    @When("카카오 로그인을 시작한다")
    public void 카카오_로그인을_시작한다() {
        // 리다이렉트를 따라가면 실제 카카오로 요청이 나간다.
        response = restTemplate
            .withRequestFactorySettings(ClientHttpRequestFactorySettings.defaults().withRedirects(Redirects.DONT_FOLLOW))
            .getForEntity("/api/auth/kakao/login", Void.class);
    }

    @Then("카카오 동의 화면으로 이동한다")
    public void 카카오_동의_화면으로_이동한다() {
        assertThat(response.getStatusCode().is3xxRedirection()).isTrue();
        UriComponents location = location();
        assertThat(location.getScheme() + "://" + location.getHost() + location.getPath())
            .isEqualTo("https://kauth.kakao.com/oauth/authorize");
    }

    @Then("이메일 제공과 카카오톡 메시지 전송 동의를 요청한다")
    public void 이메일_제공과_카카오톡_메시지_전송_동의를_요청한다() {
        String scope = location().getQueryParams().getFirst("scope");
        assertThat(Arrays.asList(scope.split(","))).contains("account_email", "talk_message");
    }

    private UriComponents location() {
        return UriComponentsBuilder.fromUri(response.getHeaders().getLocation()).build();
    }
}
