package ninja.ranner.conductor.adapter.out.terminal;

import org.jline.terminal.Size;

public interface RenderTarget {
    Size size();

    void update(Lines lines);
}
