package commands;

import java.util.HashMap;

public abstract class Command implements Runnable {

    protected HashMap<String, Object> parameters;

    final public void init(HashMap<String, Object> parameters) {
        this.parameters = parameters;
    }

    protected abstract void execute();

    final public void run() {
        this.execute();
    }

}
