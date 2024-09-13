package ninja.ranner.conductor.adapter.out.process;

import java.util.Random;

public class TerminalCommand implements CommandList, Runnable {

    private String currentCommand = "";
    private Random random;

    private final SystemPrinter printer;

    public TerminalCommand(SystemPrinter printer) {
        this.printer = printer;
    }

    public String EnterCommand() {
        // note: this would use the System.in.input stuff
        // String enteredCommand = System.in.readLine(); // kind of thing
        // return enteredCommand;
        currentCommand = System.console().readLine();
        return currentCommand;
    }
    public boolean hasNext() {
        return !currentCommand.equals("exit");
    }

    @Override
    public void run() {
        do {
            random = new Random();
            printer.print("Hello World: " + random.nextInt());
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            if (currentCommand.equals("exit")) {
                return;
            }
        } while(true);
    }
}
