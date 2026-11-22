package gift.acceptance;

import com.jayway.jsonpath.JsonPath;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 관리자 상품 화면. 로그인 없이 열리는 지금 동작(안건 #1)에 기댄다.
 */
public class AdminProductSteps {
    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private AcceptanceClient client;

    private ResponseEntity<String> response;

    @When("관리자가 상품 관리 화면을 연다")
    public void 관리자가_상품_관리_화면을_연다() {
        response = restTemplate.getForEntity("/admin/products", String.class);
    }

    @Given("관리자가 {string} 상품을 {int}원, {string} 이미지, {string} 카테고리로 추가한다")
    public void 관리자가_상품을_추가한다(String name, int price, String imageUrl, String categoryName) {
        response = restTemplate.postForEntity("/admin/products", form(name, price, imageUrl, categoryName), String.class);
    }

    @When("관리자가 {string} 상품을 {int}원, {string} 이미지, {string} 카테고리로 수정한다")
    public void 관리자가_상품을_수정한다(String name, int price, String imageUrl, String categoryName) {
        response = restTemplate.postForEntity(
            "/admin/products/" + client.productId(name) + "/edit",
            form(name, price, imageUrl, categoryName),
            String.class
        );
    }

    @When("관리자가 {string} 상품을 삭제한다")
    public void 관리자가_상품을_삭제한다(String name) {
        response = restTemplate.postForEntity(
            "/admin/products/" + client.productId(name) + "/delete", null, String.class);
    }

    @Then("화면에 다음 상품이 모두 보인다")
    public void 화면에_다음_상품이_모두_보인다(List<String> names) {
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        names.forEach(name -> assertThat(response.getBody()).contains("<td>" + name + "</td>"));
    }

    @Then("{string} 상품은 {int}원, {string} 이미지, {string} 카테고리다")
    public void 상품은_이다(String name, int price, String imageUrl, String categoryName) {
        String product = restTemplate.getForObject("/api/products/" + client.productId(name), String.class);
        assertThat((Integer) JsonPath.read(product, "$.price")).isEqualTo(price);
        assertThat((String) JsonPath.read(product, "$.imageUrl")).isEqualTo(imageUrl);
        assertThat(((Number) JsonPath.read(product, "$.categoryId")).longValue())
            .isEqualTo(client.categoryId(categoryName));
    }

    @Then("{string} 상품은 목록에 없다")
    public void 상품은_목록에_없다(String name) {
        assertThat(client.productNames()).doesNotContain(name);
    }

    @Then("상품 추가 화면에 오류가 보인다")
    public void 상품_추가_화면에_오류가_보인다() {
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).contains("<h1>상품 추가</h1>").contains("alert-danger");
    }

    @Then("상품은 {int}개 그대로다")
    public void 상품은_개_그대로다(int count) {
        assertThat(client.productNames()).hasSize(count);
    }

    private MultiValueMap<String, Object> form(String name, int price, String imageUrl, String categoryName) {
        MultiValueMap<String, Object> form = new LinkedMultiValueMap<>();
        form.add("name", name);
        form.add("price", String.valueOf(price));
        form.add("imageUrl", imageUrl);
        form.add("categoryId", String.valueOf(client.categoryId(categoryName)));
        return form;
    }
}
