package betterquesting.api2.client.gui.resources.factories.textures;

import betterquesting.api.api.ApiReference;
import betterquesting.api.api.QuestingAPI;
import betterquesting.api.utils.JsonHelper;
import betterquesting.api2.client.gui.misc.GuiPadding;
import betterquesting.api2.client.gui.resources.colors.GuiColorStatic;
import betterquesting.api2.client.gui.resources.colors.IGuiColor;
import betterquesting.api2.client.gui.resources.textures.ColorTexture;
import betterquesting.api2.client.gui.resources.textures.IGuiTexture;
import betterquesting.api2.registry.IFactoryData;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minecraft.util.ResourceLocation;

public class FactoryColorTexture implements IFactoryData<IGuiTexture, JsonObject>
{
    public static final FactoryColorTexture INSTANCE = new FactoryColorTexture();
    
    private static final ResourceLocation RES_ID = new ResourceLocation("betterquesting", "texture_color");
    
    @Override
    public ColorTexture loadFromData(JsonObject data)
    {
        int[] bounds = new int[]{0,0,0,0};
        JsonArray jAry = JsonHelper.GetArray(data, "padding");
        for(int i = 0; i < jAry.size() && i < bounds.length; i++)
        {
            if(!(jAry.get(i).isJsonPrimitive())) continue;
            try
            {
                bounds[i] = jAry.get(i).getAsInt();
            } catch(Exception ignored){}
        }
    
        IGuiColor color;
        JsonObject jCol = JsonHelper.GetObject(data, "color");
        try
        {
            IFactoryData<IGuiColor,JsonObject> tFact = QuestingAPI.getAPI(ApiReference.RESOURCE_REG).getColorReg().getFactory(new ResourceLocation(JsonHelper.GetString(jCol, "colorType", "null")));
            color = tFact.loadFromData(jCol);
            if(color == null) color = new GuiColorStatic(0xFFFFFFFF);
        } catch(Exception ignored)
        {
            color = new GuiColorStatic(0xFFFFFFFF);
        }
        
        return new ColorTexture(color, new GuiPadding(bounds[0], bounds[1], bounds[2], bounds[3]));
    }
    
    @Override
    public ResourceLocation getRegistryName()
    {
        return RES_ID;
    }
    
    @Override
    public ColorTexture createNew()
    {
        return new ColorTexture(new GuiColorStatic(0xFFFFFFFF), new GuiPadding(0, 0, 0, 0));
    }
}
