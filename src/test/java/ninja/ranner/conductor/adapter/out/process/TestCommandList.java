package ninja.ranner.conductor.adapter.out.process;

import java.util.List;
import java.util.Optional;

public class TestCommandList extends TerminalCommand {
    private final List<String> commands;

    public TestCommandList(List<String> commands) {
        super(new SpyPrinter());
        this.commands = commands;
    }

    @Override
    public String EnterCommand() {
        Optional<String> first = commands.stream().findFirst();
        commands.removeFirst();
        return first.orElse("forgot to exit");
    }

    @Override
    public boolean hasNext() {
        return commands.stream().findFirst().orElse("forgot to exit").equals("exit");
    }

    @Override
    public void run() {

    }
}
