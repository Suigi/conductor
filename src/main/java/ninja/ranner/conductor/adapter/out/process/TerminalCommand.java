package ninja.ranner.conductor.adapter.out.process;

import java.util.ArrayList;
import java.util.List;

public class TerminalCommand implements CommandList {

    private String currentCommand = "";

    public String EnterCommand() {
        // note: this would use the System.in.input stuff
        // String enteredCommand = System.in.readLine(); // kind of thing
        // return enteredCommand;
        currentCommand = "exit";
        return currentCommand;
    }
    public boolean hasNext() {
        return !currentCommand.equals("exit");
    }

}
