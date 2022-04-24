package betterquesting.client.themes;

import betterquesting.api2.client.gui.resources.colors.IGuiColor;
import betterquesting.api2.client.gui.resources.factories.colors.FactoryColorPulse;
import betterquesting.api2.client.gui.resources.factories.colors.FactoryColorSequence;
import betterquesting.api2.client.gui.resources.factories.colors.FactoryColorStatic;
import betterquesting.api2.client.gui.resources.factories.lines.FactoryLineTaxiCab;
import betterquesting.api2.client.gui.resources.factories.lines.FactorySimpleLine;
import betterquesting.api2.client.gui.resources.factories.textures.*;
import betterquesting.api2.client.gui.resources.lines.IGuiLine;
import betterquesting.api2.client.gui.resources.textures.IGuiTexture;
import betterquesting.api2.client.gui.themes.IResourceReg;
import betterquesting.api2.registry.FunctionRegistry;
import betterquesting.api2.registry.IFactoryData;
import com.google.gson.JsonObject;

public class ResourceRegistry implements IResourceReg {
    public static final ResourceRegistry INSTANCE = new ResourceRegistry();

    private final FunctionRegistry<IGuiTexture, JsonObject> TEX_REG = new FunctionRegistry<>();
    private final FunctionRegistry<IGuiColor, JsonObject> COL_REG = new FunctionRegistry<>();
    private final FunctionRegistry<IGuiLine, JsonObject> LIN_REG = new FunctionRegistry<>();

    public ResourceRegistry() {
        // NOTE: Only going to cover the basics here. Advanced GUI elements would be better suited to code based themes
        lazyRegister(TEX_REG, FactorySimpleTexture.INSTANCE, new JsonObject());
        lazyRegister(TEX_REG, FactorySlicedTexture.INSTANCE, new JsonObject());
        lazyRegister(TEX_REG, FactoryLayeredTexture.INSTANCE, new JsonObject());
        lazyRegister(TEX_REG, FactoryColorTexture.INSTANCE, new JsonObject());
        lazyRegister(TEX_REG, FactoryEmptyTexture.INSTANCE, new JsonObject());
        lazyRegister(TEX_REG, FactoryPolyTextureR.INSTANCE, new JsonObject());
        lazyRegister(TEX_REG, FactoryPolyTextureC.INSTANCE, new JsonObject());

        lazyRegister(COL_REG, FactoryColorStatic.INSTANCE, new JsonObject());
        lazyRegister(COL_REG, FactoryColorSequence.INSTANCE, new JsonObject());
        lazyRegister(COL_REG, FactoryColorPulse.INSTANCE, new JsonObject());

        lazyRegister(LIN_REG, FactorySimpleLine.INSTANCE, new JsonObject());
        lazyRegister(LIN_REG, FactoryLineTaxiCab.INSTANCE, new JsonObject());
    }

    private <T, E> void lazyRegister(FunctionRegistry<T, E> reg, IFactoryData<T, E> factory, E template) {
        reg.register(factory.getRegistryName(), factory::loadFromData, template);
    }

    @Override
    public FunctionRegistry<IGuiTexture, JsonObject> getTexReg() {
        return TEX_REG;
    }

    @Override
    public FunctionRegistry<IGuiColor, JsonObject> getColorReg() {
        return COL_REG;
    }

    @Override
    public FunctionRegistry<IGuiLine, JsonObject> getLineReg() {
        return LIN_REG;
    }

    // TODO: Sounds?
}
