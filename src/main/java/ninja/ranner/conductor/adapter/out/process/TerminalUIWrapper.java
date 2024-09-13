package ninja.ranner.conductor.adapter.out.process;

public class TerminalUIWrapper {

    private final SystemPrinter systemPrinter;
    private final CommandList commands;

    public TerminalUIWrapper(SystemPrinter systemPrinter, CommandList commands) {
        this.systemPrinter = systemPrinter;
        this.commands = commands;
    }

    public void run() {
        do {
            systemPrinter.Print("Hello Ensemblers");

            String firstCommand = commands.EnterCommand();
            systemPrinter.Print("Entered Command: " + firstCommand);
        } while (commands.hasNext());
    }

}
