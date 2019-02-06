package betterquesting.client.themes;

import betterquesting.api.client.gui.misc.IGuiHook;
import betterquesting.api2.client.gui.resources.colors.IGuiColor;
import betterquesting.api2.client.gui.resources.lines.IGuiLine;
import betterquesting.api2.client.gui.resources.textures.IGuiTexture;
import betterquesting.api2.client.gui.themes.IGuiTheme;
import net.minecraft.util.ResourceLocation;

import java.util.HashMap;

public class ResourceTheme implements IGuiTheme
{
    private final ResourceLocation ID;
    private final IGuiTheme parentTheme;
    private final String dispName;
    
	private final HashMap<ResourceLocation, IGuiTexture> TEX_MAP = new HashMap<>();
	private final HashMap<ResourceLocation, IGuiColor> COLOR_MAP = new HashMap<>();
	private final HashMap<ResourceLocation, IGuiLine> LINE_MAP = new HashMap<>();
    
    public ResourceTheme(IGuiTheme parent, ResourceLocation id, String dispName)
    {
        IGuiTheme checking = parent;
        while(checking != null)
        {
            if(checking instanceof ResourceTheme)
            {
                if(((ResourceTheme)checking).parentTheme == this) throw new IllegalArgumentException("Circular reference in resource theme " + id);
                checking = ((ResourceTheme)checking).parentTheme;
                continue;
            }
            break;
        }
        
        this.parentTheme = parent;
        this.ID = id;
        this.dispName = dispName;
    }
    
    public void setTexture(ResourceLocation key, IGuiTexture texture)
    {
        if(key == null) return;
        TEX_MAP.put(key, texture); // Could actually use null here to override the parent
    }
    
    public void setColor(ResourceLocation key, IGuiColor color)
    {
        if(key == null) return;
        COLOR_MAP.put(key, color); // Could actually use null here to override the parent
    }
    
    public void setLine(ResourceLocation key, IGuiLine line)
    {
        if(key == null) return;
        LINE_MAP.put(key, line); // Could actually use null here to override the parent
    }
    
    @Override
    public String getName()
    {
        return dispName;
    }
    
    @Override
    public ResourceLocation getID()
    {
        return ID;
    }
    
    @Override
    public IGuiTexture getTexture(ResourceLocation key)
    {
        IGuiTexture value = TEX_MAP.get(key);
        if(value != null) return value;
        if(parentTheme != null) return parentTheme.getTexture(key);
        return null;
    }
    
    @Override
    public IGuiLine getLine(ResourceLocation key)
    {
        IGuiLine value = LINE_MAP.get(key);
        if(value != null) return value;
        if(parentTheme != null) return parentTheme.getLine(key);
        return null;
    }
    
    @Override
    public IGuiColor getColor(ResourceLocation key)
    {
        IGuiColor value = COLOR_MAP.get(key);
        if(value != null) return value;
        if(parentTheme != null) return parentTheme.getColor(key);
        return null;
    }
    
    @Override
    public IGuiHook getGuiHook()
    {
        // Resource themes obviously can't define something as complex as GUI so we'll let the parent deal with that
        return parentTheme == null ? null : parentTheme.getGuiHook();
    }
}
