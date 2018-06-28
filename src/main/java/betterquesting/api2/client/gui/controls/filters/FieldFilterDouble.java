package betterquesting.api2.client.gui.controls.filters;

import betterquesting.api2.client.gui.controls.IFieldFilter;

public class FieldFilterDouble implements IFieldFilter<Double>
{
    public static final FieldFilterDouble INSTANCE = new FieldFilterDouble();
    
    @Override
    public boolean isValid(String input)
    {
        try
        {
            Double.parseDouble(input);
        } catch(Exception e)
        {
            return false;
        }
        
        return true;
    }
    
    @Override
    public String filterText(String input)
    {
        return input.replaceAll("[^.0123456789-]", "");
    }
    
    @Override
    public Double parseValue(String input)
    {
        try
        {
            return Double.parseDouble(input);
        } catch(Exception e)
        {
            return 0.0D;
        }
    }
}