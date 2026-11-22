package gift.acceptance;

import com.jayway.jsonpath.JsonPath;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class WishSteps {
    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private AcceptanceClient client;

    @Autowired
    private LoggedInMember loggedInMember;

    private ResponseEntity<String> response;

    @When("{string} 상품을 위시에 담는다")
    public void 상품을_위시에_담는다(String productName) {
        addWish(client.productId(productName));
    }

    @When("없는 상품을 위시에 담는다")
    public void 없는_상품을_위시에_담는다() {
        addWish(999999L);
    }

    @Then("{string} 상품이 위시에 담긴다")
    public void 상품이_위시에_담긴다(String productName) {
        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat((String) JsonPath.read(response.getBody(), "$.name")).isEqualTo(productName);
    }

    @Then("오류 없이 기존 {string} 항목을 보여 준다")
    public void 오류_없이_기존_항목을_보여_준다(String productName) {
        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        List<Number> existingIds = JsonPath.read(myWishes(), "$.content[?(@.name == '" + productName + "')].id");
        Number shownId = JsonPath.read(response.getBody(), "$.id");
        assertThat(existingIds).hasSize(1);
        assertThat(shownId.longValue()).isEqualTo(existingIds.get(0).longValue());
    }

    @Then("위시에 담기지 않는다")
    public void 위시에_담기지_않는다() {
        assertThat(response.getStatusCode().is2xxSuccessful()).isFalse();
    }

    @Then("내 위시에는 다음 상품이 빠짐없이 있다")
    public void 내_위시에는_다음_상품이_빠짐없이_있다(List<String> productNames) {
        List<String> actual = JsonPath.read(myWishes(), "$.content[*].name");
        assertThat(actual).containsExactlyInAnyOrderElementsOf(productNames);
    }

    private void addWish(long productId) {
        response = restTemplate.postForEntity(
            "/api/wishes", loggedInMember.request(Map.of("productId", productId)), String.class);
    }

    private String myWishes() {
        return restTemplate.exchange(
            "/api/wishes?size=100", HttpMethod.GET, loggedInMember.request(null), String.class).getBody();
    }
}
