package betterquesting.client.ui_builder;

import betterquesting.abs.misc.GuiAnchor;
import betterquesting.api2.client.gui.misc.GuiAlign;
import betterquesting.api2.client.gui.misc.GuiPadding;
import betterquesting.api2.client.gui.misc.GuiTransform;
import betterquesting.api2.client.gui.panels.IGuiPanel;
import betterquesting.api2.storage.INBTSaveLoad;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.nbt.StringNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.StringUtils;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

public class ComponentPanel implements INBTSaveLoad<CompoundNBT>
{
    // Purely for organisational purposes
    public String refName = "New Panel";
    public String panelType = "betterquesting:canvas_empty";
    
    // Usually these two are the same but not always
    public int cvParentID = -1; // ID of the canvas we're contained within
    public int tfParentID = -1; // ID of the transform we're positioned relative to
    
    private CompoundNBT transTag = new CompoundNBT();
    private CompoundNBT panelData = new CompoundNBT();
    
    // When these are passed off to the GUI context, make sure it's stated whether it's in-editor or not
    // (only content and navigation need setting up otherwise the GUI might actually edit things before intended use)
    private final List<String> scripts = new ArrayList<>();
    
    public ComponentPanel()
    {
        setTransform(new GuiTransform(GuiAlign.FULL_BOX, new GuiPadding(8, 8, 8, 8), 0));
    }
    
    public ComponentPanel(GuiTransform transform)
    {
        setTransform(transform);
    }
    
    public void setTransform(GuiTransform transform)
    {
        GuiAnchor anchor = transform.getAnchor();
        transTag.putFloat("anchor_left", anchor.getX());
        transTag.putFloat("anchor_top", anchor.getY());
        transTag.putFloat("anchor_right", anchor.getZ());
        transTag.putFloat("anchor_bottom", anchor.getW());
        
        GuiPadding padding = transform.getPadding();
        transTag.putInt("pad_left", padding.l);
        transTag.putInt("pad_top", padding.t);
        transTag.putInt("pad_right", padding.r);
        transTag.putInt("pad_bottom", padding.b);
        
        transTag.putInt("depth", transform.getDepth());
    }
    
    public CompoundNBT getTransformTag()
    {
        return transTag;
    }
    
    public CompoundNBT getPanelData()
    {
        return panelData;
    }
    
    public void setPanelData(@Nonnull CompoundNBT tag)
    {
        this.panelData = tag;
    }
    
    public IGuiPanel build()
    {
        GuiAnchor anchor = new GuiAnchor(transTag.getFloat("anchor_left"), transTag.getFloat("anchor_top"), transTag.getFloat("anchor_right"), transTag.getFloat("anchor_bottom"));
        GuiPadding padding = new GuiPadding(transTag.getInt("pad_left"), transTag.getInt("pad_top"), transTag.getInt("pad_right"), transTag.getInt("pad_bottom"));
        GuiTransform transform = new GuiTransform(anchor, padding, transTag.getInt("depth"));
        
        ResourceLocation res = StringUtils.isNullOrEmpty(panelType) ? new ResourceLocation("betterquesting:canvas_empty") : new ResourceLocation(panelType);
        return ComponentRegistry.INSTANCE.createNew(res, transform, panelData);
    }
    
    @Override
    public CompoundNBT writeToNBT(CompoundNBT nbt)
    {
        nbt.putString("ref_name", refName);
        nbt.putString("panel_type", panelType);
        
        nbt.putInt("cv_parent", cvParentID);
        nbt.putInt("tf_parent", tfParentID);
        
        nbt.put("transform", transTag.copy());
        nbt.put("panel_data", panelData.copy());
        
        ListNBT sList = new ListNBT();
        scripts.forEach((str) -> sList.add(new StringNBT(str)));
        nbt.put("script_hooks", sList);
        
        return nbt;
    }
    
    @Override
    public void readFromNBT(CompoundNBT nbt)
    {
        refName = nbt.getString("ref_name");
        panelType = nbt.getString("panel_type");
        
        cvParentID = nbt.getInt("cv_parent");
        tfParentID = nbt.getInt("tf_parent");
        
        // Location of the panel
        transTag = nbt.getCompound("transform").copy();
        panelData = nbt.getCompound("panel_data").copy();
        
        scripts.clear();
        ListNBT sList = nbt.getList("script_hooks", 8);
        for(int i = 0; i < sList.size(); i++)
        {
            scripts.add(sList.getString(i));
        }
    }
}
