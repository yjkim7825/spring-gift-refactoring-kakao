package gift.acceptance;

import com.jayway.jsonpath.JsonPath;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.stereotype.Component;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 여러 스텝 클래스가 함께 쓰는 조회. 사용자가 보는 이름으로 번호를 찾는다.
 */
@Component
public class AcceptanceClient {
    @Autowired
    private TestRestTemplate restTemplate;

    public long productId(String productName) {
        String products = restTemplate.getForObject("/api/products?size=100", String.class);
        return single(JsonPath.read(products, "$.content[?(@.name == '" + productName + "')].id"));
    }

    public long optionId(String productName, String optionName) {
        String options = restTemplate.getForObject(
            "/api/products/" + productId(productName) + "/options", String.class);
        return single(JsonPath.read(options, "$[?(@.name == '" + optionName + "')].id"));
    }

    private long single(List<Number> ids) {
        assertThat(ids).hasSize(1);
        return ids.get(0).longValue();
    }
}
