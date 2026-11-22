package gift.acceptance;

import io.cucumber.java.en.Then;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;

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

    @Then("{string} 회원의 포인트는 {int}이다")
    public void 회원의_포인트는_이다(String email, int point) {
        assertThat(memberPoints()).containsEntry(email, point);
    }

    private Map<String, Integer> memberPoints() {
        String page = restTemplate.getForObject("/admin/members", String.class);
        Map<String, Integer> points = new LinkedHashMap<>();
        Matcher row = MEMBER_ROW.matcher(page);
        while (row.find()) {
            points.put(row.group(2), Integer.parseInt(row.group(3)));
        }
        return points;
    }
}
