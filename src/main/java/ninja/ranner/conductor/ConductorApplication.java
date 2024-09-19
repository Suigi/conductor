package ninja.ranner.conductor;

import ninja.ranner.conductor.adapter.out.terminal.Lines;
import ninja.ranner.conductor.adapter.out.terminal.TerminalUi;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class ConductorApplication {

    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm:ss");
    private static String lastCommand = "";

    public static void main(String[] args) throws Exception {
        TerminalUi tui = TerminalUi.create();

        tui.registerCommandHandler(cmd -> {
            lastCommand = cmd;
            draw(tui);
        });
        draw(tui);
        try (AutoCloseable ignored = oncePerSecond(() -> draw(tui))) {
            tui.run();
        }
    }

    private static void draw(TerminalUi tui) {
        Lines lines = Lines.of(
                "Welcome, Ensemblers!",
                "",
                "Time: %s".formatted(LocalTime.now().format(TIME_FORMAT))
        );
        if (!lastCommand.isEmpty()) {
            lines.append("", "You said: " + lastCommand);
        }
        tui.update(lines);
    }

    private static AutoCloseable oncePerSecond(Runnable command) {
        ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
        scheduledExecutorService.scheduleAtFixedRate(
                command,
                1,
                1,
                TimeUnit.SECONDS
        );
        return scheduledExecutorService;
    }
}
