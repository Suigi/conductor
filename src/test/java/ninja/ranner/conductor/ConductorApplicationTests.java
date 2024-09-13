package ninja.ranner.conductor;

import ninja.ranner.conductor.adapter.out.process.TerminalUIWrapper;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.shell.component.view.TerminalUI;
import org.springframework.shell.component.view.TerminalUIBuilder;
import org.springframework.shell.component.view.control.BoxView;
import org.springframework.shell.component.view.screen.DefaultScreen;
import org.springframework.shell.component.view.screen.Screen;
import org.springframework.shell.geom.Rectangle;
import org.springframework.shell.test.ShellScreenAssert;
import org.springframework.test.context.TestPropertySource;

import static org.assertj.core.api.Assertions.assertThat;

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
        var terminalUIBuilder = new TerminalUI(new T)
        var ui = terminalUIBuilder.build();
        var boxView = new BoxView();
        boxView.setShowBorder(true);
        boxView.setDrawFunction((screen, rectangle) -> something(screen, rectangle));
        ui.configure(boxView);
        Screen screen = new DefaultScreen(10, 80);
        Rectangle result = ConductorApplication.something(screen, new Rectangle(0, 0, 10, 80));
        System.out.println();



    }
//    @Test
//    void AnotherTest() {
//
//        TerminalUIWrapper terminalUIWrapper = new TerminalUIWrapper();
//        terminalUIWrapper.run();
//
//        assertThat(terminalUIWrapper.retrieveMessage(0))
//                .isEqualTo("Hello, Ensemblers");
//    }
}
