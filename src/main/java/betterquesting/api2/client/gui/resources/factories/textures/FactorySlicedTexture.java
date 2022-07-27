package betterquesting.api2.client.gui.resources.factories.textures;

import betterquesting.api.utils.JsonHelper;
import betterquesting.api2.client.gui.misc.GuiPadding;
import betterquesting.api2.client.gui.misc.GuiRectangle;
import betterquesting.api2.client.gui.resources.textures.IGuiTexture;
import betterquesting.api2.client.gui.resources.textures.SlicedTexture;
import betterquesting.api2.client.gui.resources.textures.SlicedTexture.SliceMode;
import betterquesting.api2.client.gui.themes.presets.PresetTexture;
import betterquesting.api2.registry.IFactoryData;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minecraft.util.MathHelper;
import net.minecraft.util.ResourceLocation;

public class FactorySlicedTexture implements IFactoryData<IGuiTexture, JsonObject> {
    public static final FactorySlicedTexture INSTANCE = new FactorySlicedTexture();

    private static final ResourceLocation RES_ID = new ResourceLocation("betterquesting", "texture_sliced");

    @Override
    public SlicedTexture loadFromData(JsonObject data) {
        ResourceLocation atlas =
                new ResourceLocation(JsonHelper.GetString(data, "atlas", PresetTexture.TX_NULL.toString()));
        int sliceMode = MathHelper.clamp_int(
                JsonHelper.GetNumber(data, "sliceMode", 0).intValue(), 0, SliceMode.values().length);

        int[] bounds = new int[] {0, 0, 16, 16};
        JsonArray jAry = JsonHelper.GetArray(data, "bounds");
        for (int i = 0; i < jAry.size() && i < bounds.length; i++) {
            if (!(jAry.get(i).isJsonPrimitive())) continue;
            try {
                bounds[i] = jAry.get(i).getAsInt();
            } catch (Exception ignored) {
            }
        }

        int[] padding = new int[] {0, 0, 16, 16};
        JsonArray jAry2 = JsonHelper.GetArray(data, "padding");
        for (int i = 0; i < jAry.size() && i < padding.length; i++) {
            if (!(jAry2.get(i).isJsonPrimitive())) continue;
            try {
                padding[i] = jAry2.get(i).getAsInt();
            } catch (Exception ignored) {
            }
        }

        return new SlicedTexture(
                        atlas,
                        new GuiRectangle(bounds[0], bounds[1], bounds[2], bounds[3]),
                        new GuiPadding(padding[0], padding[1], padding[2], padding[3]))
                .setSliceMode(SliceMode.values()[sliceMode]);
    }

    @Override
    public ResourceLocation getRegistryName() {
        return RES_ID;
    }

    @Override
    public SlicedTexture createNew() {
        return new SlicedTexture(PresetTexture.TX_NULL, new GuiRectangle(0, 0, 16, 16), new GuiPadding(1, 1, 1, 1));
    }
}
