package betterquesting.api2.client.gui.themes;

import betterquesting.api2.client.gui.resources.colors.IGuiColor;
import betterquesting.api2.client.gui.resources.lines.IGuiLine;
import betterquesting.api2.client.gui.resources.textures.IGuiTexture;
import betterquesting.api2.registry.IFactoryJSON;
import betterquesting.api2.registry.IRegistry;
import com.google.gson.JsonObject;

/** Registry for BQ GUI resource type loaders. Used primarily for loading themes from JSON */
public interface IResourceReg
{
    IRegistry<IFactoryJSON<IGuiTexture, JsonObject>, IGuiTexture> getTexReg();
    IRegistry<IFactoryJSON<IGuiColor, JsonObject>, IGuiColor> getColorReg();
    IRegistry<IFactoryJSON<IGuiLine, JsonObject>, IGuiLine> getLineReg();
}
