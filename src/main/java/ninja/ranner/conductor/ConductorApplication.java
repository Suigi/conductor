package ninja.ranner.conductor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.shell.component.message.ShellMessageBuilder;
import org.springframework.shell.component.view.TerminalUIBuilder;
import org.springframework.shell.component.view.control.BoxView;
import org.springframework.shell.component.view.event.KeyEvent;
import org.springframework.shell.component.view.screen.Screen;
import org.springframework.shell.geom.HorizontalAlign;
import org.springframework.shell.geom.Rectangle;
import org.springframework.shell.geom.VerticalAlign;

import java.io.IOError;

@SpringBootApplication
public class ConductorApplication {

    Logger logger = LoggerFactory.getLogger(ConductorApplication.class);

    public static void main(String[] args) {
        SpringApplication.run(ConductorApplication.class, args);
    }

    @Bean
    @ConditionalOnProperty(value = "spring.shell.interactive.enabled", havingValue = "true", matchIfMissing = true)
    CommandLineRunner renderUi(TerminalUIBuilder terminalUIBuilder) {
        return (String... args) -> {
            var ui = terminalUIBuilder.build();

            var boxView = new BoxView();
            boxView.setShowBorder(true);
            boxView.setDrawFunction((screen, rectangle) -> something(screen, rectangle));
            ui.configure(boxView);
            ui.setRoot(boxView, true);

            var eventLoop = ui.getEventLoop();
            eventLoop.keyEvents().subscribe(keyEvent -> {
                if (keyEvent.getPlainKey() == KeyEvent.Key.q) {
                    eventLoop.dispatch(ShellMessageBuilder.ofInterrupt());
                }
            });

            try {
                ui.run();
            } catch (IOError e) {
                logger.info("Caught IOError: {}", e.getMessage());
                // ignore
            }
        };
    }

    public static Rectangle something(Screen screen, Rectangle rectangle) {
        var writer = screen.writerBuilder().build();
        writer.text("Hello, Ensemblers", rectangle, HorizontalAlign.CENTER, VerticalAlign.CENTER);
        writer.text(" p: Push, f: Pull, q: Quit ", 2, rectangle.height() - 1);
        return rectangle;
    }

}
