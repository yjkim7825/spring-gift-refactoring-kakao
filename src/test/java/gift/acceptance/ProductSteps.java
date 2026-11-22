package gift.acceptance;

import com.jayway.jsonpath.JsonPath;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class ProductSteps {
    @Autowired
    private TestRestTemplate restTemplate;

    private ResponseEntity<String> response;

    @When("손님이 상품 목록을 한 페이지에 {int}개씩 나눠 첫 페이지를 본다")
    public void 손님이_상품_목록을_나눠_첫_페이지를_본다(int size) {
        response = restTemplate.getForEntity("/api/products?page=0&size=" + size, String.class);
    }

    @When("손님이 상품 목록을 가격이 싼 순서로 본다")
    public void 손님이_상품_목록을_가격이_싼_순서로_본다() {
        response = restTemplate.getForEntity("/api/products?size=100&sort=price,asc", String.class);
    }

    @When("손님이 {string} 상품의 상세를 본다")
    public void 손님이_상품의_상세를_본다(String name) {
        String products = restTemplate.getForObject("/api/products?size=100", String.class);
        List<Integer> ids = JsonPath.read(products, "$.content[?(@.name == '" + name + "')].id");
        assertThat(ids).hasSize(1);
        response = restTemplate.getForEntity("/api/products/" + ids.get(0), String.class);
    }

    @When("손님이 없는 상품의 상세를 본다")
    public void 손님이_없는_상품의_상세를_본다() {
        response = restTemplate.getForEntity("/api/products/999999", String.class);
    }

    @Then("상품 {int}개가 보인다")
    public void 상품이_보인다(int count) {
        List<Object> content = JsonPath.read(response.getBody(), "$.content");
        assertThat(content).hasSize(count);
    }

    @Then("전체 상품은 {int}개이고 모두 {int}페이지다")
    public void 전체_상품은_개이고_모두_페이지다(int totalElements, int totalPages) {
        Integer actualTotalElements = JsonPath.read(response.getBody(), "$.totalElements");
        Integer actualTotalPages = JsonPath.read(response.getBody(), "$.totalPages");
        assertThat(actualTotalElements).isEqualTo(totalElements);
        assertThat(actualTotalPages).isEqualTo(totalPages);
    }

    @Then("상품은 다음 순서로 보인다")
    public void 상품은_다음_순서로_보인다(List<String> names) {
        List<String> actual = JsonPath.read(response.getBody(), "$.content[*].name");
        assertThat(actual).containsExactlyElementsOf(names);
    }

    @Then("상품 상세는 다음과 같다")
    public void 상품_상세는_다음과_같다(DataTable table) {
        Map<String, String> expected = table.asMaps().get(0);
        String body = response.getBody();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(Map.of(
            "이름", JsonPath.read(body, "$.name"),
            "가격", String.valueOf((Integer) JsonPath.read(body, "$.price")),
            "이미지", JsonPath.read(body, "$.imageUrl")
        )).isEqualTo(expected);
    }

    @Then("카테고리 번호는 {string} 카테고리의 번호다")
    public void 카테고리_번호는_카테고리의_번호다(String categoryName) {
        String categories = restTemplate.getForObject("/api/categories", String.class);
        List<Integer> categoryIds = JsonPath.read(categories, "$[?(@.name == '" + categoryName + "')].id");
        Integer categoryId = JsonPath.read(response.getBody(), "$.categoryId");
        assertThat(categoryIds).containsExactly(categoryId);
    }

    @Then("상품을 찾을 수 없다")
    public void 상품을_찾을_수_없다() {
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }
}
