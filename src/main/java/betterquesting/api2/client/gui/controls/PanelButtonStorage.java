package betterquesting.api2.client.gui.controls;

import betterquesting.api.misc.ICallback;
import betterquesting.api2.client.gui.misc.IGuiRect;

public class PanelButtonStorage<T> extends PanelButton {
  private T stored = null;
  private ICallback<T> callback = null;

  public PanelButtonStorage(IGuiRect rect, int id, String txt, T value) {
    super(rect, id, txt);
    setStoredValue(value);
  }

  public PanelButtonStorage<T> setStoredValue(T value) {
    stored = value;
    return this;
  }

  public T getStoredValue() {
    return stored;
  }

  public PanelButtonStorage<T> setCallback(ICallback<T> callback) {
    this.callback = callback;
    return this;
  }

  public ICallback<T> getCallback() {
    return callback;
  }

  @Override
  public void onButtonClick() {
    if (callback != null) {
      callback.setValue(getStoredValue());
    }
  }
}
