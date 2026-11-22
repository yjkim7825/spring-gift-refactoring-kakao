package gift.acceptance;

import com.jayway.jsonpath.JsonPath;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class AdminCategorySteps {
    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private AcceptanceClient client;

    @When("{string} 카테고리를 {string} 색상, {string} 이미지, {string} 설명으로 만든다")
    public void 카테고리를_만든다(String name, String color, String imageUrl, String description) {
        restTemplate.postForEntity("/api/categories", category(name, color, imageUrl, description), String.class);
    }

    @Given("{string} 카테고리를 {string} 색상, {string} 이미지로 설명 없이 만든다")
    public void 카테고리를_설명_없이_만든다(String name, String color, String imageUrl) {
        restTemplate.postForEntity("/api/categories", category(name, color, imageUrl, null), String.class);
    }

    @When("{string} 카테고리를 {string} 이름, {string} 색상, {string} 이미지, {string} 설명으로 고친다")
    public void 카테고리를_고친다(String name, String newName, String color, String imageUrl, String description) {
        restTemplate.exchange(
            "/api/categories/" + client.categoryId(name),
            HttpMethod.PUT,
            new HttpEntity<>(category(newName, color, imageUrl, description)),
            String.class
        );
    }

    @When("{string} 카테고리를 지운다")
    public void 카테고리를_지운다(String name) {
        restTemplate.delete("/api/categories/" + client.categoryId(name));
    }

    @Then("카테고리 목록에 {string} 카테고리가 {string} 색상으로 있다")
    public void 카테고리_목록에_카테고리가_있다(String name, String color) {
        List<String> colors = JsonPath.read(categories(), "$[?(@.name == '" + name + "')].color");
        assertThat(colors).containsExactly(color);
    }

    @Then("카테고리 목록에 {string} 카테고리가 없다")
    public void 카테고리_목록에_카테고리가_없다(String name) {
        List<String> names = JsonPath.read(categories(), "$[*].name");
        assertThat(names).doesNotContain(name);
    }

    @Then("카테고리는 {int}개 그대로다")
    public void 카테고리는_개_그대로다(int count) {
        List<Object> all = JsonPath.read(categories(), "$");
        assertThat(all).hasSize(count);
    }

    private Map<String, Object> category(String name, String color, String imageUrl, String description) {
        Map<String, Object> body = new HashMap<>();
        body.put("name", name);
        body.put("color", color);
        body.put("imageUrl", imageUrl);
        body.put("description", description);
        return body;
    }

    private String categories() {
        return restTemplate.getForObject("/api/categories", String.class);
    }
}
