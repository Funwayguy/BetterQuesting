package betterquesting.client.themes;

import betterquesting.api2.client.gui.resources.colors.IGuiColor;
import betterquesting.api2.client.gui.resources.factories.colors.FactoryColorPulse;
import betterquesting.api2.client.gui.resources.factories.colors.FactoryColorSequence;
import betterquesting.api2.client.gui.resources.factories.colors.FactoryColorStatic;
import betterquesting.api2.client.gui.resources.factories.lines.FactorySimpleLine;
import betterquesting.api2.client.gui.resources.factories.textures.*;
import betterquesting.api2.client.gui.resources.lines.IGuiLine;
import betterquesting.api2.client.gui.resources.textures.IGuiTexture;
import betterquesting.api2.client.gui.themes.IResourceReg;
import betterquesting.api2.registry.IFactoryData;
import betterquesting.api2.registry.IRegistry;
import betterquesting.api2.registry.SimpleRegistry;
import com.google.gson.JsonObject;

public class ResourceRegistry implements IResourceReg
{
    public static final ResourceRegistry INSTANCE = new ResourceRegistry();
    
    private final IRegistry<IFactoryData<IGuiTexture, JsonObject>, IGuiTexture> TEX_REG = new SimpleRegistry<>();
    private final IRegistry<IFactoryData<IGuiColor, JsonObject>, IGuiColor> COL_REG = new SimpleRegistry<>();
    private final IRegistry<IFactoryData<IGuiLine, JsonObject>, IGuiLine> LIN_REG = new SimpleRegistry<>();
    
    public ResourceRegistry()
    {
        // NOTE: Only going to cover the basics here. Advanced GUI elements would be better suited to code based themes
        TEX_REG.register(FactorySimpleTexture.INSTANCE);
        TEX_REG.register(FactorySlicedTexture.INSTANCE);
        TEX_REG.register(FactoryLayeredTexture.INSTANCE);
        TEX_REG.register(FactorySlideShowTexture.INSTANCE);
        TEX_REG.register(FactoryColorTexture.INSTANCE);
        
        COL_REG.register(FactoryColorStatic.INSTANCE);
        COL_REG.register(FactoryColorSequence.INSTANCE);
        COL_REG.register(FactoryColorPulse.INSTANCE);
        
        LIN_REG.register(FactorySimpleLine.INSTANCE);
    }
    
    @Override
    public IRegistry<IFactoryData<IGuiTexture, JsonObject>, IGuiTexture> getTexReg()
    {
        return TEX_REG;
    }
    
    @Override
    public IRegistry<IFactoryData<IGuiColor, JsonObject>, IGuiColor> getColorReg()
    {
        return COL_REG;
    }
    
    @Override
    public IRegistry<IFactoryData<IGuiLine, JsonObject>, IGuiLine> getLineReg()
    {
        return LIN_REG;
    }
    
    // TODO: Sounds?
}
