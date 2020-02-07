package betterquesting.api2.client.gui.resources.factories.textures;

import betterquesting.api.api.ApiReference;
import betterquesting.api.api.QuestingAPI;
import betterquesting.api.utils.JsonHelper;
import betterquesting.api2.client.gui.resources.colors.GuiColorStatic;
import betterquesting.api2.client.gui.resources.colors.IGuiColor;
import betterquesting.api2.client.gui.resources.textures.IGuiTexture;
import betterquesting.api2.client.gui.resources.textures.PolyTexture;
import betterquesting.api2.registry.IFactoryData;
import betterquesting.core.BetterQuesting;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minecraft.util.ResourceLocation;

public class FactoryPolyTextureC implements IFactoryData<IGuiTexture, JsonObject>
{
    public static final FactoryPolyTextureC INSTANCE = new FactoryPolyTextureC();
    private static final ResourceLocation ID_NAME = new ResourceLocation(BetterQuesting.MODID, "poly_custom");
    
    @Override
    public IGuiTexture loadFromData(JsonObject data)
    {
        JsonArray jAry = JsonHelper.GetArray(data, "verts");
        double[] verts = new double[jAry.size()];
        for(int i = 0; i < jAry.size() && i < verts.length; i++)
        {
            if(!(jAry.get(i).isJsonPrimitive())) continue;
            try
            {
                verts[i] = jAry.get(i).getAsDouble();
            } catch(Exception ignored){}
        }
        
        boolean shadow = JsonHelper.GetBoolean(data, "shadow", true);
        
        IGuiColor color;
        JsonObject jCol = JsonHelper.GetObject(data, "color");
        try
        {
            color = QuestingAPI.getAPI(ApiReference.RESOURCE_REG).getColorReg().createNew(new ResourceLocation(JsonHelper.GetString(jCol, "colorType", "null")), jCol);
            if(color == null) color = new GuiColorStatic(0xFFFFFFFF);
        } catch(Exception ignored)
        {
            color = new GuiColorStatic(0xFFFFFFFF);
        }
        
        int borderSize = JsonHelper.GetNumber(data, "borderSize", 0).intValue();
        IGuiColor borColor;
        jCol = JsonHelper.GetObject(data, "borderColor");
        try
        {
            borColor = QuestingAPI.getAPI(ApiReference.RESOURCE_REG).getColorReg().createNew(new ResourceLocation(JsonHelper.GetString(jCol, "colorType", "null")), jCol);
            if(borColor == null) borColor = new GuiColorStatic(0xFFFFFFFF);
        } catch(Exception ignored)
        {
            borColor = new GuiColorStatic(0xFFFFFFFF);
        }
        
        
        return new PolyTexture(verts, shadow, color).setBorder(borderSize, borColor);
    }
    
    @Override
    public ResourceLocation getRegistryName()
    {
        return ID_NAME;
    }
    
    @Override
    public IGuiTexture createNew()
    {
        return new PolyTexture(4, 45D, true, new GuiColorStatic(0xFFFFFFFF));
    }
}
