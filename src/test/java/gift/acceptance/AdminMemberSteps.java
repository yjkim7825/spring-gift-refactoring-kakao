package gift.acceptance;

import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 관리자 회원 화면. 로그인 없이 열리는 지금 동작(안건 #1)에 기댄다.
 */
public class AdminMemberSteps {
    // 회원 목록 표의 한 줄: 번호, 이메일, 포인트
    private static final Pattern MEMBER_ROW =
        Pattern.compile("<td>(\\d+)</td>\\s*<td>([^<]+)</td>\\s*<td>(-?\\d+)</td>");

    @Autowired
    private TestRestTemplate restTemplate;

    private ResponseEntity<String> response;

    @Given("관리자가 {string} 이메일과 {string} 비밀번호로 회원을 추가한다")
    public void 관리자가_회원을_추가한다(String email, String password) {
        response = restTemplate.postForEntity("/admin/members", form("email", email, "password", password), String.class);
    }

    @When("관리자가 {string} 회원을 {string} 이메일과 {string} 비밀번호로 수정한다")
    public void 관리자가_회원을_수정한다(String email, String newEmail, String password) {
        response = restTemplate.postForEntity(
            "/admin/members/" + memberId(email) + "/edit", form("email", newEmail, "password", password), String.class);
    }

    @When("관리자가 {string} 회원을 삭제한다")
    public void 관리자가_회원을_삭제한다(String email) {
        response = restTemplate.postForEntity("/admin/members/" + memberId(email) + "/delete", null, String.class);
    }

    @When("관리자가 {string} 회원에게 {int}포인트를 충전한다")
    public void 관리자가_회원에게_포인트를_충전한다(String email, int amount) {
        response = restTemplate.postForEntity(
            "/admin/members/" + memberId(email) + "/charge-point", form("amount", String.valueOf(amount)), String.class);
    }

    @Then("회원 목록은 다음과 같다")
    public void 회원_목록은_다음과_같다(DataTable table) {
        Map<String, String> expected = new LinkedHashMap<>();
        table.asMaps().forEach(row -> expected.put(row.get("이메일"), row.get("포인트")));
        Map<String, String> actual = new LinkedHashMap<>();
        members().forEach((email, row) -> actual.put(email, String.valueOf(row[1])));
        assertThat(actual).isEqualTo(expected);
    }

    @Then("회원 목록에 {string} 회원이 있다")
    public void 회원_목록에_회원이_있다(String email) {
        assertThat(members()).containsKey(email);
    }

    @Then("회원 목록에 {string} 회원이 없다")
    public void 회원_목록에_회원이_없다(String email) {
        assertThat(members()).doesNotContainKey(email);
    }

    @Then("회원 추가 화면에 오류가 보인다")
    public void 회원_추가_화면에_오류가_보인다() {
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).contains("<h1>회원 추가</h1>").contains("alert-danger");
    }

    @Then("회원은 {int}명 그대로다")
    public void 회원은_명_그대로다(int count) {
        assertThat(members()).hasSize(count);
    }

    @Then("{string} 회원의 포인트는 {int}이다")
    public void 회원의_포인트는_이다(String email, int point) {
        assertThat(members()).containsKey(email);
        assertThat(members().get(email)[1]).isEqualTo(point);
    }

    private long memberId(String email) {
        assertThat(members()).containsKey(email);
        return members().get(email)[0];
    }

    // 이메일마다 [번호, 포인트]
    private Map<String, long[]> members() {
        String page = restTemplate.getForObject("/admin/members", String.class);
        Map<String, long[]> members = new LinkedHashMap<>();
        Matcher row = MEMBER_ROW.matcher(page);
        while (row.find()) {
            members.put(row.group(2), new long[] {Long.parseLong(row.group(1)), Long.parseLong(row.group(3))});
        }
        return members;
    }

    private MultiValueMap<String, Object> form(String... keyValues) {
        MultiValueMap<String, Object> form = new LinkedMultiValueMap<>();
        for (int i = 0; i < keyValues.length; i += 2) {
            form.add(keyValues[i], keyValues[i + 1]);
        }
        return form;
    }
}
