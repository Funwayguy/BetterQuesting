package betterquesting.api2.client.gui.resources.factories.textures;

import betterquesting.api.api.ApiReference;
import betterquesting.api.api.QuestingAPI;
import betterquesting.api.utils.JsonHelper;
import betterquesting.api2.client.gui.misc.GuiRectangle;
import betterquesting.api2.client.gui.resources.textures.IGuiTexture;
import betterquesting.api2.client.gui.resources.textures.LayeredTexture;
import betterquesting.api2.client.gui.resources.textures.SimpleTexture;
import betterquesting.api2.client.gui.themes.presets.PresetTexture;
import betterquesting.api2.registry.IFactoryData;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.util.ResourceLocation;

public class FactoryLayeredTexture implements IFactoryData<IGuiTexture, JsonObject> {
    public static final FactoryLayeredTexture INSTANCE = new FactoryLayeredTexture();

    private static final ResourceLocation RES_ID = new ResourceLocation("betterquesting", "texture_layered");
    private static final IGuiTexture NULL_TX =
            new SimpleTexture(PresetTexture.TX_NULL, new GuiRectangle(0, 0, 16, 16)).maintainAspect(false);

    @Override
    public LayeredTexture loadFromData(JsonObject data) {
        List<IGuiTexture> layers = new ArrayList<>();

        JsonArray jAry = JsonHelper.GetArray(data, "layers");
        for (JsonElement je : jAry) {
            if (!je.isJsonObject()) continue;
            JsonObject jo = je.getAsJsonObject();

            try {
                IFactoryData<IGuiTexture, JsonObject> tFact = QuestingAPI.getAPI(ApiReference.RESOURCE_REG)
                        .getTexReg()
                        .getFactory(new ResourceLocation(JsonHelper.GetString(jo, "textureType", "null")));
                layers.add(tFact.loadFromData(jo));
            } catch (Exception ignored) {
                layers.add(NULL_TX);
            }
        }

        return new LayeredTexture(layers.toArray(new IGuiTexture[0]));
    }

    @Override
    public ResourceLocation getRegistryName() {
        return RES_ID;
    }

    @Override
    public LayeredTexture createNew() {
        return new LayeredTexture();
    }
}
