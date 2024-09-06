package ninja.ranner.conductor;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@Tag("Spring")
@TestPropertySource(properties = {
        "spring.shell.interactive.enabled=false"
})
class ConductorApplicationTests {

    @Test
    void contextLoads() {
    }

}
