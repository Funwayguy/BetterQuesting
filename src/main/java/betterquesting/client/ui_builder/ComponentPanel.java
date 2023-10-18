package betterquesting.client.ui_builder;

import betterquesting.api2.client.gui.misc.GuiAlign;
import betterquesting.api2.client.gui.misc.GuiPadding;
import betterquesting.api2.client.gui.misc.GuiTransform;
import betterquesting.api2.client.gui.panels.IGuiPanel;
import betterquesting.api2.storage.INBTSaveLoad;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.StringUtils;
import org.lwjgl.util.vector.Vector4f;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

public class ComponentPanel implements INBTSaveLoad<NBTTagCompound> {
  // Purely for organisational purposes
  public String refName = "New Panel";
  public String panelType = "betterquesting:canvas_empty";

  // Usually these two are the same but not always
  public int cvParentID = -1; // ID of the canvas we're contained within
  public int tfParentID = -1; // ID of the transform we're positioned relative to

  private NBTTagCompound transTag = new NBTTagCompound();
  private NBTTagCompound panelData = new NBTTagCompound();

  // When these are passed off to the GUI context, make sure it's stated whether it's in-editor or not
  // (only content and navigation need setting up otherwise the GUI might actually edit things before intended use)
  private final List<String> scripts = new ArrayList<>();

  public ComponentPanel() {
    setTransform(new GuiTransform(GuiAlign.FULL_BOX, new GuiPadding(8, 8, 8, 8), 0));
  }

  public ComponentPanel(GuiTransform transform) {
    setTransform(transform);
  }

  public void setTransform(GuiTransform transform) {
    Vector4f anchor = transform.getAnchor();
    transTag.setFloat("anchor_left", anchor.x);
    transTag.setFloat("anchor_top", anchor.y);
    transTag.setFloat("anchor_right", anchor.z);
    transTag.setFloat("anchor_bottom", anchor.w);

    GuiPadding padding = transform.getPadding();
    transTag.setInteger("pad_left", padding.l);
    transTag.setInteger("pad_top", padding.t);
    transTag.setInteger("pad_right", padding.r);
    transTag.setInteger("pad_bottom", padding.b);

    transTag.setInteger("depth", transform.getDepth());
  }

  public NBTTagCompound getTransformTag() {
    return transTag;
  }

  public NBTTagCompound getPanelData() {
    return panelData;
  }

  public void setPanelData(@Nonnull NBTTagCompound tag) {
    panelData = tag;
  }

  public IGuiPanel build() {
    Vector4f anchor = new Vector4f(transTag.getFloat("anchor_left"), transTag.getFloat("anchor_top"),
                                   transTag.getFloat("anchor_right"), transTag.getFloat("anchor_bottom"));
    GuiPadding padding = new GuiPadding(transTag.getInteger("pad_left"), transTag.getInteger("pad_top"),
                                        transTag.getInteger("pad_right"), transTag.getInteger("pad_bottom"));
    GuiTransform transform = new GuiTransform(anchor, padding, transTag.getInteger("depth"));

    ResourceLocation res = StringUtils.isNullOrEmpty(panelType) ? new ResourceLocation("betterquesting:canvas_empty")
                                                                : new ResourceLocation(panelType);
    return ComponentRegistry.INSTANCE.createNew(res, transform, panelData);
  }

  @Override
  public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
    nbt.setString("ref_name", refName);
    nbt.setString("panel_type", panelType);

    nbt.setInteger("cv_parent", cvParentID);
    nbt.setInteger("tf_parent", tfParentID);

    nbt.setTag("transform", transTag.copy());
    nbt.setTag("panel_data", panelData.copy());

    NBTTagList sList = new NBTTagList();
    scripts.forEach((str) -> sList.appendTag(new NBTTagString(str)));
    nbt.setTag("script_hooks", sList);

    return nbt;
  }

  @Override
  public void readFromNBT(NBTTagCompound nbt) {
    refName = nbt.getString("ref_name");
    panelType = nbt.getString("panel_type");

    cvParentID = nbt.getInteger("cv_parent");
    tfParentID = nbt.getInteger("tf_parent");

    // Location of the panel
    transTag = nbt.getCompoundTag("transform").copy();
    panelData = nbt.getCompoundTag("panel_data").copy();

    scripts.clear();
    NBTTagList sList = nbt.getTagList("script_hooks", 8);
    for (int i = 0; i < sList.tagCount(); i++) {
      scripts.add(sList.getStringTagAt(i));
    }
  }
}
