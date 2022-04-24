package betterquesting.client.themes;

import betterquesting.api.utils.JsonHelper;
import betterquesting.api2.client.gui.resources.colors.IGuiColor;
import betterquesting.api2.client.gui.resources.lines.IGuiLine;
import betterquesting.api2.client.gui.resources.textures.IGuiTexture;
import betterquesting.api2.client.gui.themes.GuiKey;
import betterquesting.api2.client.gui.themes.IGuiTheme;
import betterquesting.core.BetterQuesting;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.function.Function;

public class ResourceTheme implements IGuiTheme {
    private final ResourceLocation ID;
    private final String dispName;

    private IGuiTheme parentTheme;
    private ResourceLocation parentID;
    private boolean cached = false;

    private final HashMap<ResourceLocation, IGuiTexture> TEX_MAP = new HashMap<>();
    private final HashMap<ResourceLocation, IGuiColor> COLOR_MAP = new HashMap<>();
    private final HashMap<ResourceLocation, IGuiLine> LINE_MAP = new HashMap<>();

    public ResourceTheme(ResourceLocation parentID, ResourceLocation id, String dispName) {
        this.ID = id;
        this.dispName = dispName;
        this.parentID = parentID;
    }

    public void loadFromJson(JsonObject jThm) {
        JsonObject jsonTextureRoot = JsonHelper.GetObject(jThm, "textures");
        for (Entry<String, JsonElement> entry : jsonTextureRoot.entrySet()) {
            if (!entry.getValue().isJsonObject()) continue;
            JsonObject joTex = entry.getValue().getAsJsonObject();

            ResourceLocation typeID = new ResourceLocation(JsonHelper.GetString(joTex, "textureType", ""));
            IGuiTexture gTex = ResourceRegistry.INSTANCE.getTexReg().createNew(typeID, joTex);

            if (gTex == null) {
                BetterQuesting.logger.error("Failed to load texture type " + typeID + " for theme " + ID);
                continue;
            }

            this.setTexture(new ResourceLocation(entry.getKey()), gTex);
        }

        JsonObject jsonColourRoot = JsonHelper.GetObject(jThm, "colors");
        for (Entry<String, JsonElement> entry : jsonColourRoot.entrySet()) {
            if (!(entry.getValue() instanceof JsonObject)) continue;
            JsonObject joCol = entry.getValue().getAsJsonObject();

            ResourceLocation typeID = new ResourceLocation(JsonHelper.GetString(joCol, "colorType", ""));
            IGuiColor gCol = ResourceRegistry.INSTANCE.getColorReg().createNew(typeID, joCol);

            if (gCol == null) {
                BetterQuesting.logger.error("Failed to load color type " + typeID + " for theme " + ID);
                continue;
            }

            this.setColor(new ResourceLocation(entry.getKey()), gCol);
        }

        JsonObject jsonLinesRoot = JsonHelper.GetObject(jThm, "lines");
        for (Entry<String, JsonElement> entry : jsonLinesRoot.entrySet()) {
            if (!(entry.getValue() instanceof JsonObject)) continue;
            JsonObject joLine = entry.getValue().getAsJsonObject();

            ResourceLocation typeID = new ResourceLocation(JsonHelper.GetString(joLine, "lineType", ""));
            IGuiLine gLine = ResourceRegistry.INSTANCE.getLineReg().createNew(typeID, joLine);

            if (gLine == null) {
                BetterQuesting.logger.error("Failed to load line type " + typeID + " for theme " + ID);
                continue;
            }

            this.setLine(new ResourceLocation(entry.getKey()), gLine);
        }
    }

    private IGuiTheme getParent() {
        if (cached) return parentTheme;

        IGuiTheme parent = ThemeRegistry.INSTANCE.getTheme(parentID);
        IGuiTheme checking = parent;
        while (checking != null) {
            if (checking instanceof ResourceTheme) {
                if (((ResourceTheme) checking).parentTheme == this) {
                    BetterQuesting.logger.error("Circular reference in resource theme " + ID);
                    this.parentTheme = null;
                    cached = true;
                    return null;
                }
                checking = ((ResourceTheme) checking).parentTheme;
                continue;
            }
            break;
        }

        this.parentTheme = parent;
        cached = true;

        return this.parentTheme;
    }

    public void setTexture(ResourceLocation key, IGuiTexture texture) {
        if (key == null) return;
        TEX_MAP.put(key, texture); // Could actually use null here to override the parent
    }

    public void setColor(ResourceLocation key, IGuiColor color) {
        if (key == null) return;
        COLOR_MAP.put(key, color); // Could actually use null here to override the parent
    }

    public void setLine(ResourceLocation key, IGuiLine line) {
        if (key == null) return;
        LINE_MAP.put(key, line); // Could actually use null here to override the parent
    }

    @Override
    public String getName() {
        return dispName;
    }

    @Override
    public ResourceLocation getID() {
        return ID;
    }

    @Override
    public IGuiTexture getTexture(ResourceLocation key) {
        IGuiTexture value = TEX_MAP.get(key);
        if (value != null) return value;
        if (getParent() != null) return getParent().getTexture(key);
        return null;
    }

    @Override
    public IGuiLine getLine(ResourceLocation key) {
        IGuiLine value = LINE_MAP.get(key);
        if (value != null) return value;
        if (getParent() != null) return getParent().getLine(key);
        return null;
    }

    @Override
    public IGuiColor getColor(ResourceLocation key) {
        IGuiColor value = COLOR_MAP.get(key);
        if (value != null) return value;
        if (getParent() != null) return getParent().getColor(key);
        return null;
    }

    @Nullable
    @Override
    public <T> Function<T, GuiScreen> getGui(GuiKey<T> key) {
        return null;
    }
}
