package ninja.ranner.conductor.adapter.out.process;

import java.util.Random;

public class RealPrinter implements SystemPrinter {
    public void print(String message) {
        System.out.println(message);
    }

}
