package gift.acceptance;

import com.jayway.jsonpath.JsonPath;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
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
    private long foundWishId;

    @When("{string} 상품을 위시에 담는다")
    public void 상품을_위시에_담는다(String productName) {
        addWish(client.productId(productName));
    }

    @When("없는 상품을 위시에 담는다")
    public void 없는_상품을_위시에_담는다() {
        addWish(999999L);
    }

    @When("내 위시를 본다")
    public void 내_위시를_본다() {
        response = wishes("size=100");
    }

    @When("내 위시를 한 페이지에 {int}개씩 나눠 첫 페이지를 본다")
    public void 내_위시를_나눠_첫_페이지를_본다(int size) {
        response = wishes("page=0&size=" + size);
    }

    @Then("위시 항목 {int}개가 보인다")
    public void 위시_항목이_보인다(int count) {
        List<Object> content = JsonPath.read(response.getBody(), "$.content");
        assertThat(content).hasSize(count);
    }

    @Then("전체 위시 항목은 {int}개이고 모두 {int}페이지다")
    public void 전체_위시_항목은_개이고_모두_페이지다(int totalElements, int totalPages) {
        Integer actualTotalElements = JsonPath.read(response.getBody(), "$.totalElements");
        Integer actualTotalPages = JsonPath.read(response.getBody(), "$.totalPages");
        assertThat(actualTotalElements).isEqualTo(totalElements);
        assertThat(actualTotalPages).isEqualTo(totalPages);
    }

    @Then("위시 항목은 다음과 같다")
    public void 위시_항목은_다음과_같다(DataTable table) {
        List<Map<String, Object>> content = JsonPath.read(response.getBody(), "$.content");
        List<Map<String, String>> actual = content.stream()
            .map(wish -> Map.of(
                "이름", String.valueOf(wish.get("name")),
                "가격", String.valueOf(wish.get("price")),
                "이미지", String.valueOf(wish.get("imageUrl"))
            ))
            .toList();
        assertThat(actual).containsExactlyInAnyOrderElementsOf(table.asMaps());
    }

    @Given("이 회원의 위시에서 {string} 항목을 찾아 둔다")
    public void 이_회원의_위시에서_항목을_찾아_둔다(String productName) {
        foundWishId = myWishId(productName);
    }

    @When("내 위시에서 {string} 항목을 뺀다")
    public void 내_위시에서_항목을_뺀다(String productName) {
        removeWish(myWishId(productName));
    }

    @When("찾아 둔 다른 회원의 위시 항목을 뺀다")
    public void 찾아_둔_다른_회원의_위시_항목을_뺀다() {
        removeWish(foundWishId);
    }

    @Then("위시에서 빠진다")
    public void 위시에서_빠진다() {
        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
    }

    @Then("권한이 없다며 거절된다")
    public void 권한이_없다며_거절된다() {
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
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

    private long myWishId(String productName) {
        List<Number> ids = JsonPath.read(myWishes(), "$.content[?(@.name == '" + productName + "')].id");
        assertThat(ids).hasSize(1);
        return ids.get(0).longValue();
    }

    private void removeWish(long wishId) {
        response = restTemplate.exchange(
            "/api/wishes/" + wishId, HttpMethod.DELETE, loggedInMember.request(null), String.class);
    }

    private String myWishes() {
        return wishes("size=100").getBody();
    }

    private ResponseEntity<String> wishes(String query) {
        return restTemplate.exchange(
            "/api/wishes?" + query, HttpMethod.GET, loggedInMember.request(null), String.class);
    }
}
