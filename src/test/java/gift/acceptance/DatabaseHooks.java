package gift.acceptance;

import io.cucumber.java.Before;
import org.flywaydb.core.Flyway;
import org.springframework.beans.factory.annotation.Autowired;

public class DatabaseHooks {
    @Autowired
    private Flyway flyway;

    @Before
    public void 초기_데이터로_되돌린다() {
        flyway.clean();
        flyway.migrate();
    }
}
