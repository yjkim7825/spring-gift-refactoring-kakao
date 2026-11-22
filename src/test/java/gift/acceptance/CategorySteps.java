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

public class CategorySteps {
    @Autowired
    private TestRestTemplate restTemplate;

    private ResponseEntity<List<Map<String, Object>>> response;

    @When("손님이 카테고리 목록을 본다")
    public void 손님이_카테고리_목록을_본다() {
        response = restTemplate.exchange(
            "/api/categories",
            HttpMethod.GET,
            null,
            new ParameterizedTypeReference<>() {
            }
        );
    }

    @Then("카테고리 목록이 보인다")
    public void 카테고리_목록이_보인다() {
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotEmpty();
    }

    @Then("다음 카테고리가 빠짐없이 보인다")
    public void 다음_카테고리가_빠짐없이_보인다(List<String> names) {
        assertThat(response.getBody())
            .extracting(category -> category.get("name"))
            .containsExactlyInAnyOrderElementsOf(names);
    }

    @Then("각 카테고리의 정보는 다음과 같다")
    public void 각_카테고리의_정보는_다음과_같다(DataTable table) {
        List<Map<String, Object>> actual = response.getBody().stream()
            .map(category -> Map.of(
                "이름", category.get("name"),
                "대표 색상", category.get("color"),
                "이미지", category.get("imageUrl"),
                "설명", category.get("description")
            ))
            .toList();
        assertThat(actual).containsExactlyInAnyOrderElementsOf(table.asMaps(String.class, Object.class));
    }
}
