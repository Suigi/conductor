package ninja.ranner.conductor.adapter.out.process;

import java.util.ArrayList;
import java.util.List;

public class SpyPrinter implements SystemPrinter {
    private List<String> messages = new ArrayList<String>();
    @Override
    public void print(String message) {
        messages.add(message);
    }
    public String RetrieveMessage(int index) {
        return messages.get(index);
    }
}
