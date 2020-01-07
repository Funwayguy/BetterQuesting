package betterquesting.api2.client.gui.resources.factories.lines;

import betterquesting.api.utils.JsonHelper;
import betterquesting.api2.client.gui.resources.lines.IGuiLine;
import betterquesting.api2.client.gui.resources.lines.SimpleLine;
import betterquesting.api2.registry.IFactoryData;
import com.google.gson.JsonObject;
import net.minecraft.util.ResourceLocation;

public class FactorySimpleLine implements IFactoryData<IGuiLine, JsonObject>
{
    public static final FactorySimpleLine INSTANCE = new FactorySimpleLine();
    
    private static final ResourceLocation RES_ID = new ResourceLocation("betterquesting", "line_simple");
    
    @Override
    public SimpleLine loadFromData(JsonObject data)
    {
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
        
        return new SimpleLine(stippleScale, stippleMask);
    }
    
    @Override
    public ResourceLocation getRegistryName()
    {
        return RES_ID;
    }
    
    @Override
    public SimpleLine createNew()
    {
        return new SimpleLine();
    }
}