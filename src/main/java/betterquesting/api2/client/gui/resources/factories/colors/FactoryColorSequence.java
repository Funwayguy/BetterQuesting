package betterquesting.api2.client.gui.resources.factories.colors;

import betterquesting.api.api.ApiReference;
import betterquesting.api.api.QuestingAPI;
import betterquesting.api.utils.JsonHelper;
import betterquesting.api2.client.gui.resources.colors.GuiColorSequence;
import betterquesting.api2.client.gui.resources.colors.GuiColorStatic;
import betterquesting.api2.client.gui.resources.colors.IGuiColor;
import betterquesting.api2.registry.IFactoryData;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.util.ResourceLocation;

import java.util.ArrayList;
import java.util.List;

public class FactoryColorSequence implements IFactoryData<IGuiColor, JsonObject>
{
    public static final FactoryColorSequence INSTANCE = new FactoryColorSequence();
    
    private static final ResourceLocation RES_ID = new ResourceLocation("betterquesting", "color_sequence");
    private static final IGuiColor NULL_COL = new GuiColorStatic(0xFFFFFFFF);
    
    @Override
    public GuiColorSequence loadFromData(JsonObject data)
    {
        List<IGuiColor> layers = new ArrayList<>();
        
        float interval = JsonHelper.GetNumber(data, "interval", 1F).floatValue();
        
        JsonArray jAry = JsonHelper.GetArray(data, "colors");
        for(JsonElement je : jAry)
        {
            if(!je.isJsonObject()) continue;
            JsonObject jo = je.getAsJsonObject();
            
            try
            {
                IFactoryData<IGuiColor,JsonObject> tFact = QuestingAPI.getAPI(ApiReference.RESOURCE_REG).getColorReg().getFactory(new ResourceLocation(JsonHelper.GetString(jo, "colorType", "null")));
                layers.add(tFact.loadFromData(jo));
            } catch(Exception ignored)
            {
                layers.add(NULL_COL);
            }
        }
        
        return new GuiColorSequence(interval, layers.toArray(new IGuiColor[0]));
    }
    
    @Override
    public ResourceLocation getRegistryName()
    {
        return RES_ID;
    }
    
    @Override
    public GuiColorSequence createNew()
    {
        return new GuiColorSequence(1F);
    }
}