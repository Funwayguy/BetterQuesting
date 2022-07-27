package betterquesting.api2.client.gui.controls.callbacks;

import betterquesting.api.misc.ICallback;

public class CallbackMulti<T> implements ICallback<T> {
    private final ICallback<T>[] callbacks;

    public CallbackMulti(ICallback<T>... callbacks) {
        this.callbacks = callbacks;
    }

    @Override
    public void setValue(T value) {
        for (ICallback<T> c : callbacks) {
            c.setValue(value);
        }
    }
}
