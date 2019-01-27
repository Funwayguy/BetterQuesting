package betterquesting.client.themes;

import betterquesting.api2.client.gui.resources.colors.IGuiColor;
import betterquesting.api2.client.gui.resources.lines.IGuiLine;
import betterquesting.api2.client.gui.resources.textures.IGuiTexture;
import betterquesting.api2.client.gui.themes.IResourceReg;
import betterquesting.api2.registry.IFactoryJSON;
import betterquesting.api2.registry.IRegistry;
import betterquesting.api2.registry.SimpleRegistry;
import com.google.gson.JsonObject;

public class ResourceRegistry implements IResourceReg
{
    public static final ResourceRegistry INSTANCE = new ResourceRegistry();
    
    private final IRegistry<IFactoryJSON<IGuiTexture, JsonObject>, IGuiTexture> TEX_REG = new SimpleRegistry<>();
    private final IRegistry<IFactoryJSON<IGuiColor, JsonObject>, IGuiColor> COL_REG = new SimpleRegistry<>();
    private final IRegistry<IFactoryJSON<IGuiLine, JsonObject>, IGuiLine> LIN_REG = new SimpleRegistry<>();
    
    @Override
    public IRegistry<IFactoryJSON<IGuiTexture, JsonObject>, IGuiTexture> getTexReg()
    {
        return TEX_REG;
    }
    
    @Override
    public IRegistry<IFactoryJSON<IGuiColor, JsonObject>, IGuiColor> getColorReg()
    {
        return COL_REG;
    }
    
    @Override
    public IRegistry<IFactoryJSON<IGuiLine, JsonObject>, IGuiLine> getLineReg()
    {
        return LIN_REG;
    }
}
