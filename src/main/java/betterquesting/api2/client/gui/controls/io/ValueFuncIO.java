package betterquesting.api2.client.gui.controls.io;

import betterquesting.api2.client.gui.controls.IValueIO;

import java.util.concurrent.Callable;

public class ValueFuncIO<T> implements IValueIO<T> {
    private final Callable<T> v;

    public ValueFuncIO(Callable<T> value) {
        this.v = value;
    }

    @Override
    public T readValue() {
        try {
            return v.call();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public void writeValue(T value) {
    }

    @Override
    public T readValueRaw() {
        return readValue();
    }

    @Override
    public void writeValueRaw(T value) {
    }
}
