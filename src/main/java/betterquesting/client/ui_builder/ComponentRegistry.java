package betterquesting.client.ui_builder;

import betterquesting.api2.client.gui.controls.PanelButton;
import betterquesting.api2.client.gui.misc.IGuiRect;
import betterquesting.api2.client.gui.panels.CanvasTextured;
import betterquesting.api2.client.gui.panels.IGuiPanel;
import betterquesting.api2.client.gui.themes.presets.PresetTexture;
import betterquesting.client.themes.ThemeRegistry;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.function.BiFunction;

public class ComponentRegistry
{
    public static final ComponentRegistry INSTANCE = new ComponentRegistry();
    
    private final HashMap<ResourceLocation, BiFunction<IGuiRect, CompoundNBT,IGuiPanel>> REG_MAP = new HashMap<>();
    private final HashMap<ResourceLocation, CompoundNBT> TEMPLATE_TAGS = new HashMap<>();
    
    public ComponentRegistry()
    {
        init();
    }
    
    public void register(@Nonnull ResourceLocation idname, @Nonnull BiFunction<IGuiRect,CompoundNBT,IGuiPanel> factory, @Nonnull CompoundNBT template)
    {
        if(REG_MAP.containsKey(idname))
        {
            throw new IllegalArgumentException("Tried to register duplicate GUI component ID");
        }
        
        REG_MAP.put(idname, factory);
        TEMPLATE_TAGS.put(idname, template);
    }
    
    @Nonnull
    public IGuiPanel createNew(@Nonnull ResourceLocation idName, @Nonnull IGuiRect rect, @Nullable CompoundNBT tag)
    {
        BiFunction<IGuiRect,CompoundNBT,IGuiPanel> factory = REG_MAP.get(idName);
        if(factory == null) return new CanvasTextured(rect, ThemeRegistry.INSTANCE.getTexture(null)); // TODO: Return placeholder panel
        IGuiPanel pan = factory.apply(rect, tag);
        //if(tag != null) pan.readFromNBT(tag);
        return pan;
    }
    
    @Nonnull
    public CompoundNBT getTemplateTag(@Nonnull ResourceLocation idName)
    {
        CompoundNBT tag = TEMPLATE_TAGS.get(idName);
        return tag == null ? new CompoundNBT() : tag.copy();
    }
    
    public List<ResourceLocation> getRegisteredIDs()
    {
        return new ArrayList<>(REG_MAP.keySet());
    }
    
    private void init()
    {
        //register(new ResourceLocation("betterquesting", "canvas_empty"), CanvasEmpty::new, new NBTTagCompound());
        
        CompoundNBT refTag = new CompoundNBT();
        //refTag.setString("texture", PresetTexture.PANEL_MAIN.getKey().toString());
        register(new ResourceLocation("betterquesting", "canvas_textured"), (rect, tag) -> new CanvasTextured(rect, PresetTexture.PANEL_MAIN.getTexture()), refTag);
        register(new ResourceLocation("betterquesting", "panel_button"), (rect, tag) -> {return new PanelButton(rect, -1, "New Button");}, refTag);
    }
}
