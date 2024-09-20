package ninja.ranner.conductor;

import ninja.ranner.conductor.adapter.in.clock.Scheduler;
import ninja.ranner.conductor.adapter.out.terminal.Lines;
import ninja.ranner.conductor.adapter.out.terminal.TerminalUi;
import ninja.ranner.conductor.adapter.out.terminal.TimerTransformer;
import ninja.ranner.conductor.domain.Timer;

import java.util.concurrent.TimeUnit;

public class ConductorApplication {

    private static String lastCommand = "";
    private static final Timer timer = new Timer(5);

    public static void main(String[] args) throws Exception {
        TerminalUi tui = TerminalUi.create();

        tui.registerCommandHandler(cmd -> {
            lastCommand = cmd;
            renderAppState(tui);
        });
        renderAppState(tui);
        try (AutoCloseable ignored = new Scheduler(TimeUnit.SECONDS).oncePerSecond(() -> {
            timer.tick();
            renderAppState(tui);
        })) {
            tui.run();
        }
    }

    private static void renderAppState(TerminalUi tui) {
        Lines lines = new TimerTransformer(timer).transform();
        if (!lastCommand.isEmpty()) {
            lines.append("", "You said: " + lastCommand);
        }
        tui.update(lines);
    }

}
