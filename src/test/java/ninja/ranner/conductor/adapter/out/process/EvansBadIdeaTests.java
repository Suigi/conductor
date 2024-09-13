package ninja.ranner.conductor.adapter.out.process;

import org.junit.jupiter.api.Test;
import org.springframework.util.Assert;

import java.util.ArrayList;
import java.util.List;

public class EvansBadIdeaTests {
    @Test
    public void FirstTest() {
        ArrayList<String> commands = new ArrayList<>();
        commands.add("exit");
        TestCommandList testCommandList = new TestCommandList(commands);
        SpyPrinter spyPrinter = new SpyPrinter();
        TerminalUIWrapper terminalUIWrapper = new TerminalUIWrapper(spyPrinter, testCommandList);

        terminalUIWrapper.run();

        Assert.hasText(spyPrinter.RetrieveMessage(0), "Hello Ensemblers");
        Assert.hasText(spyPrinter.RetrieveMessage(1), "Entered Command: exit");
    }
}
