package betterquesting.api2.client.gui.controls.filters;

import betterquesting.api2.client.gui.controls.IFieldFilter;

public class FieldFilterString implements IFieldFilter<String> {
    public static final FieldFilterString INSTANCE = new FieldFilterString(null);

    private final String regex;

    public FieldFilterString(String regex) {
        this.regex = null;
    }

    @Override
    public boolean isValid(String input) {
        if (regex != null) {
            return input.matches(regex);
        }

        return true;
    }

    @Override
    public String filterText(String input) {
        if (regex != null) {
            return input.replaceAll(regex, "");
        }

        return input;
    }

    @Override
    public String parseValue(String input) {
        return input;
    }
}
