package ninja.ranner.conductor;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.shell.component.view.screen.DefaultScreen;
import org.springframework.shell.component.view.screen.Screen;
import org.springframework.shell.geom.Rectangle;
import org.springframework.shell.test.ShellScreenAssert;
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

    @Test
    void pants() {
        Screen screen = new DefaultScreen(10, 80);
        Rectangle result = ConductorApplication.something(screen, new Rectangle(0, 0, 10, 80));
        System.out.println();

    }
}
