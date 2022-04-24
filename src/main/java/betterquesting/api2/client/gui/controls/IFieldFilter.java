package betterquesting.api2.client.gui.controls;

public interface IFieldFilter<T> {
    boolean isValid(String input);

    String filterText(String input);

    T parseValue(String input);
}
