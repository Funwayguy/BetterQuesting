package betterquesting.api2.client.gui.controls;

public interface IValueIO<T> {
    T readValue();

    void writeValue(T value);

    // These are necessary for things like interpolating values that need to be jumped to a specific value (such as reloading an exising GUI)
    // Just redirect these to the useual read and write methods if you don't need them.
    T readValueRaw();

    void writeValueRaw(T value);
}
