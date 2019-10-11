package betterquesting.client.ui_builder;

import betterquesting.api2.client.gui.panels.IGuiPanel;
import betterquesting.api2.registry.IFactory;
import betterquesting.api2.registry.IRegistry;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nullable;
import java.util.List;

public class ComponentRegistry implements IRegistry<IFactory<IGuiPanel>, IGuiPanel>
{
    @Override
    public void register(IFactory<IGuiPanel> factory)
    {
        
    }
    
    @Override
    public IFactory<IGuiPanel> getFactory(ResourceLocation idName)
    {
        return null;
    }
    
    @Nullable
    @Override
    public IGuiPanel createNew(ResourceLocation idName)
    {
        return null;
    }
    
    @Override
    public List<IFactory<IGuiPanel>> getAll()
    {
        return null;
    }
}
