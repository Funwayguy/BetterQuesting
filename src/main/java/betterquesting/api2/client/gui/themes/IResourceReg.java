package betterquesting.api2.client.gui.themes;

import betterquesting.api2.client.gui.resources.colors.IGuiColor;
import betterquesting.api2.client.gui.resources.lines.IGuiLine;
import betterquesting.api2.client.gui.resources.textures.IGuiTexture;
import betterquesting.api2.registry.FunctionRegistry;
import com.google.gson.JsonObject;

/**
 * Registry for BQ GUI resource type loaders. Used primarily for loading themes from JSON
 */
public interface IResourceReg {
    FunctionRegistry<IGuiTexture, JsonObject> getTexReg();

    FunctionRegistry<IGuiColor, JsonObject> getColorReg();

    FunctionRegistry<IGuiLine, JsonObject> getLineReg();
}
