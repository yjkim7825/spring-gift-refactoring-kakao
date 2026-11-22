package gift.acceptance;

import com.jayway.jsonpath.JsonPath;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class OrderSteps {
    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private AcceptanceClient client;

    @Autowired
    private LoggedInMember loggedInMember;

    private ResponseEntity<String> response;

    @When("{string} 상품의 {string} 옵션을 {int}개, {string} 메시지로 주문한다")
    public void 메시지로_주문한다(String productName, String optionName, int quantity, String message) {
        order(client.optionId(productName, optionName), quantity, message);
    }

    @When("{string} 상품의 {string} 옵션을 {int}개, 메시지 없이 주문한다")
    public void 메시지_없이_주문한다(String productName, String optionName, int quantity) {
        order(client.optionId(productName, optionName), quantity, null);
    }

    @Then("주문된다")
    public void 주문된다() {
        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
    }

    @Then("주문되지 않는다")
    public void 주문되지_않는다() {
        assertThat(response.getStatusCode().is2xxSuccessful()).isFalse();
    }

    @Then("내 주문 내역에 {string} 상품의 {string} 옵션 {int}개, {string} 메시지 주문이 있다")
    public void 내_주문_내역에_주문이_있다(String productName, String optionName, int quantity, String message) {
        long optionId = client.optionId(productName, optionName);
        List<Map<String, Object>> orders = JsonPath.read(myOrders("size=100").getBody(), "$.content");
        assertThat(orders)
            .filteredOn(order -> ((Number) order.get("optionId")).longValue() == optionId)
            .filteredOn(order -> ((Number) order.get("quantity")).intValue() == quantity)
            .filteredOn(order -> message.equals(order.get("message")))
            .hasSize(1);
    }

    private void order(long optionId, int quantity, String message) {
        Map<String, Object> body = new HashMap<>();
        body.put("optionId", optionId);
        body.put("quantity", quantity);
        body.put("message", message);
        response = restTemplate.postForEntity("/api/orders", loggedInMember.request(body), String.class);
    }

    private ResponseEntity<String> myOrders(String query) {
        return restTemplate.exchange(
            "/api/orders?" + query, HttpMethod.GET, loggedInMember.request(null), String.class);
    }
}
