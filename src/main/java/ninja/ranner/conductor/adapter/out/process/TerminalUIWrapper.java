package ninja.ranner.conductor.adapter.out.process;

public class TerminalUIWrapper {

    private final SystemPrinter systemPrinter;
    private final TerminalCommand commands;

    public TerminalUIWrapper(SystemPrinter systemPrinter, TerminalCommand commands) {
        this.systemPrinter = systemPrinter;
        this.commands = commands;
    }

    public void run() {
        new Thread(this.commands).start();
        do {
            systemPrinter.print("Hello Ensemblers\n");
            systemPrinter.print("One command to ruin them all - exit!\n");
            String firstCommand = commands.EnterCommand();
            systemPrinter.print("Entered Command: " + firstCommand);
        } while (commands.hasNext());
    }

}
