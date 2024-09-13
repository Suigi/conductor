package ninja.ranner.conductor.adapter.out.process;

import java.util.List;
import java.util.Optional;

public class TestCommandList implements CommandList {
    private final List<String> commands;

    public TestCommandList(List<String> commands) {
        this.commands = commands;
    }

    @Override
    public String EnterCommand() {
        Optional<String> first = commands.stream().findFirst();
        commands.removeFirst();
        return first.orElse("fail");
    }

    @Override
    public boolean hasNext() {
        return commands.stream().findFirst().toString().equals("exit");
    }
}
