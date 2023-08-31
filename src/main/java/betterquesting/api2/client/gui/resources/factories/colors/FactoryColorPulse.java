package betterquesting.api2.client.gui.resources.factories.colors;

import betterquesting.api.api.ApiReference;
import betterquesting.api.api.QuestingAPI;
import betterquesting.api.utils.JsonHelper;
import betterquesting.api2.client.gui.resources.colors.GuiColorPulse;
import betterquesting.api2.client.gui.resources.colors.GuiColorStatic;
import betterquesting.api2.client.gui.resources.colors.IGuiColor;
import betterquesting.api2.registry.IFactoryData;
import com.google.gson.JsonObject;
import net.minecraft.util.ResourceLocation;

public class FactoryColorPulse implements IFactoryData<IGuiColor, JsonObject> {
  public static final FactoryColorPulse INSTANCE = new FactoryColorPulse();

  private static final ResourceLocation RES_ID = new ResourceLocation("betterquesting", "color_pulse");

  @Override
  public GuiColorPulse loadFromData(JsonObject data) {
    float period = JsonHelper.GetNumber(data, "period", 1F).floatValue();
    float phase = JsonHelper.GetNumber(data, "phase", 1F).floatValue();
    IGuiColor color1;
    IGuiColor color2;

    JsonObject jo1 = JsonHelper.GetObject(data, "color1");
    color1 = QuestingAPI.getAPI(ApiReference.RESOURCE_REG).getColorReg()
                        .createNew(new ResourceLocation(JsonHelper.GetString(jo1, "colorType", "null")), jo1);
    if (color1 == null) { color1 = new GuiColorStatic(0xFFFFFFFF); }

    JsonObject jo2 = JsonHelper.GetObject(data, "color2");
    color2 = QuestingAPI.getAPI(ApiReference.RESOURCE_REG).getColorReg()
                        .createNew(new ResourceLocation(JsonHelper.GetString(jo2, "colorType", "null")), jo2);
    if (color2 == null) { color2 = new GuiColorStatic(0xFFFFFFFF); }

    return new GuiColorPulse(color1, color2, period, phase);
  }

  @Override
  public ResourceLocation getRegistryName() {
    return RES_ID;
  }

  @Override
  public GuiColorPulse createNew() {
    return new GuiColorPulse(0xFFFFFFFF, 0xFFFFFFFF, 1F, 1F);
  }
}