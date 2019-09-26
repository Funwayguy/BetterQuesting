package betterquesting.client.ui_builder;

import betterquesting.api2.client.gui.misc.GuiPadding;
import betterquesting.api2.client.gui.misc.GuiTransform;
import betterquesting.api2.client.gui.panels.CanvasTextured;
import betterquesting.api2.client.gui.panels.IGuiCanvas;
import betterquesting.api2.client.gui.panels.IGuiPanel;
import betterquesting.api2.client.gui.themes.presets.PresetTexture;
import betterquesting.api2.storage.INBTSaveLoad;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import org.lwjgl.util.vector.Vector4f;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class ComponentPanel implements INBTSaveLoad<NBTTagCompound>
{
    // Purely for organisational purposes
    public String refName = "New Panel";
    
    // Usually these two are the same but not always
    public int cvParentID = -1; // ID of the canvas we're contained within
    public int tfParentID = -1; // ID of the transform we're positioned relative to
    
    private NBTTagCompound transTag = new NBTTagCompound();
    private NBTTagCompound panelData = new NBTTagCompound();
    
    private final List<String> scripts = new ArrayList<>();
    private final List<ComponentPanel> children = new ArrayList<>();
    
    public ComponentPanel()
    {
        transTag.setFloat("anchor_left", 0F);
        transTag.setFloat("anchor_top", 0F);
        transTag.setFloat("anchor_right", 1F);
        transTag.setFloat("anchor_bottom", 1F);
        
        transTag.setInteger("pad_left", 8);
        transTag.setInteger("pad_top", 8);
        transTag.setInteger("pad_right", 8);
        transTag.setInteger("pad_bottom", 8);
        
        transTag.setInteger("depth", 0);
    }
    
    public NBTTagCompound getTransformTag()
    {
        return transTag;
    }
    
    public NBTTagCompound getPanelProperties()
    {
        return panelData;
    }
    
    public List<ComponentPanel> getChildList()
    {
        return children;
    }
    
    // TODO: Add a way to set parent transforms independently of canvases. This will likely need a database of unique ID references
    public IGuiPanel build(@Nullable IGuiCanvas parent) // Note: You should cleanup previous children before running things
    {
        Vector4f anchor = new Vector4f(transTag.getFloat("anchor_left"), transTag.getFloat("anchor_top"), transTag.getFloat("anchor_right"), transTag.getFloat("anchor_bottom"));
        GuiPadding padding = new GuiPadding(transTag.getInteger("pad_left"), transTag.getInteger("pad_top"), transTag.getInteger("pad_right"), transTag.getInteger("pad_bottom"));
        GuiTransform transform = new GuiTransform(anchor, padding, transTag.getInteger("depth"));
        
        // TODO: Look up registered factory and load in panel data
        CanvasTextured canvas = new CanvasTextured(transform, PresetTexture.PANEL_MAIN.getTexture());
        if(parent != null) parent.addPanel(canvas);
        
        //if(canvas instanceof IGuiCanvas)
        {
            children.forEach((child) -> child.build(canvas)); // build() does the parenting already so it's unnecessary to do it here
        }
        
        return canvas;
    }
    
    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound nbt)
    {
        nbt.setString("ref_name", refName);
        
        nbt.setInteger("cv_parent", cvParentID);
        nbt.setInteger("tf_parent", tfParentID);
        
        nbt.setTag("transform", transTag.copy());
        nbt.setTag("panel_data", panelData.copy());
        
        NBTTagList sList = new NBTTagList();
        scripts.forEach((str) -> sList.appendTag(new NBTTagString(str)));
        nbt.setTag("script_hooks", sList);
        
        NBTTagList cList = new NBTTagList();
        children.forEach((child) -> sList.appendTag(child.writeToNBT(new NBTTagCompound())));
        nbt.setTag("children", cList);
        
        return nbt;
    }
    
    @Override
    public void readFromNBT(NBTTagCompound nbt)
    {
        refName = nbt.getString("ref_name");
        
        cvParentID = nbt.getInteger("cv_parent");
        tfParentID = nbt.getInteger("tf_parent");
        
        // Location of the panel
        transTag = nbt.getCompoundTag("transform").copy();
        panelData = nbt.getCompoundTag("panel_data").copy();
        
        scripts.clear();
        NBTTagList sList = nbt.getTagList("script_hooks", 8);
        for(int i = 0; i < sList.tagCount(); i++)
        {
            scripts.add(sList.getStringTagAt(i));
        }
        
        children.clear();
        NBTTagList cList = nbt.getTagList("children", 10);
        for(int i = 0; i < cList.tagCount(); i++)
        {
            ComponentPanel child = new ComponentPanel();
            child.readFromNBT(cList.getCompoundTagAt(i));
            children.add(child);
        }
    }
}
