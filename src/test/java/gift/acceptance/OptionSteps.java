package gift.acceptance;

import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class OptionSteps {
    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private AcceptanceClient client;

    private ResponseEntity<List<Map<String, Object>>> response;
    private ResponseEntity<String> changeResponse;

    @When("손님이 {string} 상품의 옵션을 본다")
    public void 손님이_상품의_옵션을_본다(String productName) {
        response = options(productName);
    }

    @When("{string} 상품에 {string} 옵션을 재고 {int}개로 추가한다")
    public void 옵션을_추가한다(String productName, String optionName, int quantity) {
        changeResponse = restTemplate.postForEntity(
            "/api/products/" + client.productId(productName) + "/options",
            Map.of("name", optionName, "quantity", quantity),
            String.class
        );
    }

    @When("{string} 상품에서 {string} 옵션을 삭제한다")
    public void 옵션을_삭제한다(String productName, String optionName) {
        changeResponse = restTemplate.exchange(
            "/api/products/" + client.productId(productName) + "/options/" + client.optionId(productName, optionName),
            HttpMethod.DELETE,
            null,
            String.class
        );
    }

    @Then("옵션이 추가된다")
    public void 옵션이_추가된다() {
        assertThat(changeResponse.getStatusCode().is2xxSuccessful()).isTrue();
    }

    @Then("옵션이 추가되지 않는다")
    public void 옵션이_추가되지_않는다() {
        assertThat(changeResponse.getStatusCode().is2xxSuccessful()).isFalse();
    }

    @Then("옵션이 삭제된다")
    public void 옵션이_삭제된다() {
        assertThat(changeResponse.getStatusCode().is2xxSuccessful()).isTrue();
    }

    @Then("옵션이 삭제되지 않는다")
    public void 옵션이_삭제되지_않는다() {
        assertThat(changeResponse.getStatusCode().is2xxSuccessful()).isFalse();
    }

    @Then("{string} 상품의 옵션은 {int}개 그대로다")
    public void 상품의_옵션은_개_그대로다(String productName, int count) {
        assertThat(options(productName).getBody()).hasSize(count);
    }

    @Then("옵션은 다음과 같이 빠짐없이 보인다")
    public void 옵션은_다음과_같이_빠짐없이_보인다(DataTable table) {
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        List<Map<String, String>> actual = response.getBody().stream()
            .map(option -> Map.of(
                "옵션명", String.valueOf(option.get("name")),
                "재고", String.valueOf(option.get("quantity"))
            ))
            .toList();
        assertThat(actual).containsExactlyInAnyOrderElementsOf(table.asMaps());
    }

    @Then("{string} 상품의 {string} 옵션 재고는 {int}개다")
    public void 옵션_재고는_개다(String productName, String optionName, int quantity) {
        assertThat(options(productName).getBody())
            .filteredOn(option -> optionName.equals(option.get("name")))
            .extracting(option -> option.get("quantity"))
            .containsExactly(quantity);
    }

    private ResponseEntity<List<Map<String, Object>>> options(String productName) {
        return restTemplate.exchange(
            "/api/products/" + client.productId(productName) + "/options",
            HttpMethod.GET,
            null,
            new ParameterizedTypeReference<>() {
            }
        );
    }
}
