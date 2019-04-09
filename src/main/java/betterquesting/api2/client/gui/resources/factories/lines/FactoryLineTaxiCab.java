package betterquesting.api2.client.gui.resources.factories.lines;

import betterquesting.api.utils.JsonHelper;
import betterquesting.api2.client.gui.resources.lines.IGuiLine;
import betterquesting.api2.client.gui.resources.lines.LineTaxiCab;
import betterquesting.api2.registry.IFactoryData;
import com.google.gson.JsonObject;
import net.minecraft.util.ResourceLocation;

public class FactoryLineTaxiCab implements IFactoryData<IGuiLine, JsonObject>
{
    public static final FactoryLineTaxiCab INSTANCE = new FactoryLineTaxiCab();
    
    private static final ResourceLocation RES_ID = new ResourceLocation("betterquesting", "line_manhattan");
    
    @Override
    public LineTaxiCab loadFromData(JsonObject data)
    {
        float bias = JsonHelper.GetNumber(data, "bias", 0.5F).floatValue();
        boolean isVertical = JsonHelper.GetBoolean(data, "isVertical", false);
        
        int stippleScale = JsonHelper.GetNumber(data, "stippleScale", 1).intValue();
        short stippleMask;
        
        try
        {
            // Needs to be done through int so that the signed bit isn't dropped
            stippleMask = (short)Integer.parseInt(JsonHelper.GetString(data, "stippleMask", "1111111111111111"), 2);
        } catch(NumberFormatException ignored)
        {
            stippleMask = (short)0xFFFF;
        }
        
        return new LineTaxiCab(bias, isVertical, stippleScale, stippleMask);
    }
    
    @Override
    public ResourceLocation getRegistryName()
    {
        return RES_ID;
    }
    
    @Override
    public LineTaxiCab createNew()
    {
        return new LineTaxiCab();
    }
}
