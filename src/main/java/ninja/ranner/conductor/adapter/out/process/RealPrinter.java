package ninja.ranner.conductor.adapter.out.process;

public class RealPrinter implements SystemPrinter {
    public void Print(String message) {
        System.out.println(message);
    }
}
