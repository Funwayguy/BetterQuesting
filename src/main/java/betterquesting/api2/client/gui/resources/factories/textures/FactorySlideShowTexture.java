package betterquesting.api2.client.gui.resources.factories.textures;

import betterquesting.api.api.ApiReference;
import betterquesting.api.api.QuestingAPI;
import betterquesting.api.utils.JsonHelper;
import betterquesting.api2.client.gui.misc.GuiRectangle;
import betterquesting.api2.client.gui.resources.textures.IGuiTexture;
import betterquesting.api2.client.gui.resources.textures.SimpleTexture;
import betterquesting.api2.client.gui.resources.textures.SlideShowTexture;
import betterquesting.api2.client.gui.themes.presets.PresetTexture;
import betterquesting.api2.registry.IFactoryData;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.util.ResourceLocation;

import java.util.ArrayList;
import java.util.List;

public class FactorySlideShowTexture implements IFactoryData<IGuiTexture, JsonObject>
{
    public static final FactorySlideShowTexture INSTANCE = new FactorySlideShowTexture();
    
    private static final ResourceLocation RES_ID = new ResourceLocation("betterquesting", "texture_slides");
    private static final IGuiTexture NULL_TX = new SimpleTexture(PresetTexture.TX_NULL, new GuiRectangle(0, 0, 16, 16)).maintainAspect(false);
    
    @Override
    public SlideShowTexture loadFromData(JsonObject data)
    {
        List<IGuiTexture> layers = new ArrayList<>();
        
        float interval = JsonHelper.GetNumber(data, "interval", 1F).floatValue();
        
        JsonArray jAry = JsonHelper.GetArray(data, "slides");
        for(JsonElement je : jAry)
        {
            if(!je.isJsonObject()) continue;
            JsonObject jo = je.getAsJsonObject();
            
            try
            {
                IFactoryData<IGuiTexture,JsonObject> tFact = QuestingAPI.getAPI(ApiReference.RESOURCE_REG).getTexReg().getFactory(new ResourceLocation(JsonHelper.GetString(jo, "textureType", "null")));
                layers.add(tFact.loadFromData(jo));
            } catch(Exception ignored)
            {
                layers.add(NULL_TX);
            }
        }
        
        return new SlideShowTexture(interval, layers.toArray(new IGuiTexture[0]));
    }
    
    @Override
    public ResourceLocation getRegistryName()
    {
        return RES_ID;
    }
    
    @Override
    public SlideShowTexture createNew()
    {
        return new SlideShowTexture(1F);
    }
}
