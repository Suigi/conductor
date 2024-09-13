package ninja.ranner.conductor.adapter.out.process;

public interface CommandList {
    public String EnterCommand();
    public boolean hasNext();
    public void run();
}
