package betterquesting.api2.client.gui.controls.filters;

import betterquesting.api2.client.gui.controls.IFieldFilter;

public class FieldFilterString implements IFieldFilter<String>
{
    public static final FieldFilterString INSTANCE = new FieldFilterString();
    
    @Override
    public boolean isValid(String input)
    {
        return true;
    }
    
    @Override
    public String filterText(String input)
    {
        return input;
    }
    
    @Override
    public String parseValue(String input)
    {
        return input;
    }
}
