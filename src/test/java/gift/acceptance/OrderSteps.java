package gift.acceptance;

import com.jayway.jsonpath.JsonPath;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;

import java.util.ArrayList;
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

    @When("내 주문 내역을 본다")
    public void 내_주문_내역을_본다() {
        response = myOrders("size=100");
    }

    @When("내 주문 내역을 한 페이지에 {int}개씩 나눠 첫 페이지를 본다")
    public void 내_주문_내역을_나눠_첫_페이지를_본다(int size) {
        response = myOrders("page=0&size=" + size);
    }

    @Then("주문 {int}개가 보인다")
    public void 주문이_보인다(int count) {
        List<Object> content = JsonPath.read(response.getBody(), "$.content");
        assertThat(content).hasSize(count);
    }

    @Then("전체 주문은 {int}개이고 모두 {int}페이지다")
    public void 전체_주문은_개이고_모두_페이지다(int totalElements, int totalPages) {
        Integer actualTotalElements = JsonPath.read(response.getBody(), "$.totalElements");
        Integer actualTotalPages = JsonPath.read(response.getBody(), "$.totalPages");
        assertThat(actualTotalElements).isEqualTo(totalElements);
        assertThat(actualTotalPages).isEqualTo(totalPages);
    }

    @Then("내 주문 내역에는 다음 주문이 빠짐없이 있다")
    public void 내_주문_내역에는_다음_주문이_빠짐없이_있다(DataTable table) {
        assertThat(orderRows(List.of("optionId", "quantity")))
            .containsExactlyInAnyOrderElementsOf(expectedRows(table, List.of("수량")));
    }

    @Then("각 주문의 정보는 다음과 같다")
    public void 각_주문의_정보는_다음과_같다(DataTable table) {
        assertThat(orderRows(List.of("optionId", "quantity", "orderDateTime", "message")))
            .containsExactlyInAnyOrderElementsOf(expectedRows(table, List.of("수량", "주문 일시", "메시지")));
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

    // 응답의 각 주문을 [옵션 번호, 그 밖의 필드...] 문자열 목록으로 바꾼다
    private List<List<String>> orderRows(List<String> fields) {
        List<Map<String, Object>> orders = JsonPath.read(response.getBody(), "$.content");
        return orders.stream()
            .map(order -> fields.stream().map(field -> String.valueOf(order.get(field))).toList())
            .toList();
    }

    // 표의 상품과 옵션을 옵션 번호로 바꾸고, 나머지 열은 그대로 둔다. 빈 칸은 null이다.
    private List<List<String>> expectedRows(DataTable table, List<String> columns) {
        return table.asMaps().stream()
            .map(row -> {
                List<String> values = new ArrayList<>();
                values.add(String.valueOf(client.optionId(row.get("상품"), row.get("옵션"))));
                columns.forEach(column -> values.add(String.valueOf(row.get(column))));
                return values;
            })
            .toList();
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
