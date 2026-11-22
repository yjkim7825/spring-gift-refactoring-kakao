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

    @When("손님이 {string} 상품의 옵션을 본다")
    public void 손님이_상품의_옵션을_본다(String productName) {
        response = options(productName);
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
