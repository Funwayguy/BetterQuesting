package betterquesting.api2.client.gui.resources.factories.textures;

import betterquesting.api.utils.JsonHelper;
import betterquesting.api2.client.gui.misc.GuiRectangle;
import betterquesting.api2.client.gui.resources.textures.IGuiTexture;
import betterquesting.api2.client.gui.resources.textures.SimpleTexture;
import betterquesting.api2.client.gui.themes.presets.PresetTexture;
import betterquesting.api2.registry.IFactoryData;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minecraft.util.ResourceLocation;

public class FactorySimpleTexture implements IFactoryData<IGuiTexture, JsonObject>
{
    public static final FactorySimpleTexture INSTANCE = new FactorySimpleTexture();
    
    private static final ResourceLocation RES_ID = new ResourceLocation("betterquesting", "texture_simple");
    
    @Override
    public SimpleTexture loadFromData(JsonObject data)
    {
        ResourceLocation atlas = new ResourceLocation(JsonHelper.GetString(data, "atlas", PresetTexture.TX_NULL.toString()));
        boolean aspect = !JsonHelper.GetBoolean(data, "stretch", false);
        
        int[] bounds = new int[]{0,0,16,16};
        JsonArray jAry = JsonHelper.GetArray(data, "bounds");
        for(int i = 0; i < jAry.size() && i < bounds.length; i++)
        {
            if(!(jAry.get(i).isJsonPrimitive())) continue;
            try
            {
                bounds[i] = jAry.get(i).getAsInt();
            } catch(Exception ignored){}
        }
        
        return new SimpleTexture(atlas, new GuiRectangle(bounds[0], bounds[1], bounds[2], bounds[3])).maintainAspect(aspect);
    }
    
    @Override
    public ResourceLocation getRegistryName()
    {
        return RES_ID;
    }
    
    @Override
    public SimpleTexture createNew()
    {
        return new SimpleTexture(PresetTexture.TX_NULL, new GuiRectangle(0, 0, 16, 16));
    }
}
