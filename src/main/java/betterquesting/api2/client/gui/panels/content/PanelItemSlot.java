package betterquesting.api2.client.gui.panels.content;

import betterquesting.api.utils.BigItemStack;
import betterquesting.api2.client.gui.controls.PanelButtonStorage;
import betterquesting.api2.client.gui.misc.GuiPadding;
import betterquesting.api2.client.gui.misc.IGuiRect;
import betterquesting.api2.client.gui.resources.textures.ColorTexture;
import betterquesting.api2.client.gui.resources.textures.ItemTexture;
import betterquesting.api2.client.gui.resources.textures.LayeredTexture;
import betterquesting.api2.client.gui.resources.textures.OreDictTexture;
import betterquesting.api2.client.gui.themes.presets.PresetColor;
import betterquesting.api2.client.gui.themes.presets.PresetTexture;
import net.minecraft.client.Minecraft;
import net.minecraft.client.util.ITooltipFlag.TooltipFlags;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraftforge.oredict.OreDictionary;

import java.util.ArrayList;
import java.util.List;

public class PanelItemSlot extends PanelButtonStorage<BigItemStack> {
  private final boolean showCount;
  private final boolean oreDict;

  private final List<BigItemStack> oreVariants = new ArrayList<>();

  public PanelItemSlot(IGuiRect rect, int id, BigItemStack value) {
    this(rect, id, value, false, false);
  }

  public PanelItemSlot(IGuiRect rect, int id, BigItemStack value, boolean showCount) {
    this(rect, id, value, showCount, false);
  }

  public PanelItemSlot(IGuiRect rect, int id, BigItemStack value, boolean showCount, boolean oreDict) {
    super(rect, id, "", value);
    this.showCount = showCount;
    this.oreDict = oreDict;

    this.setTextures(PresetTexture.ITEM_FRAME.getTexture(), PresetTexture.ITEM_FRAME.getTexture(),
                     new LayeredTexture(PresetTexture.ITEM_FRAME.getTexture(),
                                        new ColorTexture(PresetColor.ITEM_HIGHLIGHT.getColor(),
                                                         new GuiPadding(1, 1, 1, 1))));
    this.setStoredValue(value); // Need to run this again because of the instatiation order of showCount
  }

  @Override
  public PanelItemSlot setStoredValue(BigItemStack value) {
    super.setStoredValue(value);

    if (value != null) {
      Minecraft mc = Minecraft.getMinecraft();
      this.setIcon(
          oreDict || value.getBaseStack().getItemDamage() == OreDictionary.WILDCARD_VALUE ? new OreDictTexture(1F,
                                                                                                               value,
                                                                                                               showCount,
                                                                                                               true)
                                                                                          : new ItemTexture(value,
                                                                                                            showCount,
                                                                                                            true), 1);
      this.setTooltip(value.getBaseStack().getTooltip(mc.player,
                                                      mc.gameSettings.advancedItemTooltips ? TooltipFlags.ADVANCED
                                                                                           : TooltipFlags.NORMAL));
    } else {
      this.setIcon(null);
      this.setTooltip(null);
    }

    updateOreStacks();

    return this;
  }

  @Override
  public List<String> getTooltip(int mx, int my) {
    if (getStoredValue() != null && getTransform().contains(mx, my)) {
      BigItemStack ttStack = getStoredValue();

      if (oreDict && !oreVariants.isEmpty()) {
        ttStack = oreVariants.get((int) (System.currentTimeMillis() / 1000D) % oreVariants.size());
      }

      Minecraft mc = Minecraft.getMinecraft();
      return ttStack.getBaseStack().getTooltip(mc.player, mc.gameSettings.advancedItemTooltips ? TooltipFlags.ADVANCED
                                                                                               : TooltipFlags.NORMAL);
    }

    return null;
  }

  private void updateOreStacks() {
    if (oreVariants == null) // Pre-instantiation check. Crashes otherwise >_>
    {
      return;
    }

    oreVariants.clear();

    BigItemStack stack = getStoredValue();
    if (stack == null) { return; }

    if (!stack.hasOreDict()) {
      if (stack.getBaseStack().getItemDamage() == OreDictionary.WILDCARD_VALUE) {
        NonNullList<ItemStack> subItems = NonNullList.create();
        stack.getBaseStack().getItem().getSubItems(CreativeTabs.SEARCH, subItems);
        subItems.forEach((is) -> {
          BigItemStack bis = new BigItemStack(is);
          bis.stackSize = stack.stackSize;
          oreVariants.add(bis);
        });
      } else {
        oreVariants.add(stack);
      }
      return;
    }

    for (ItemStack iStack : stack.getOreIngredient().getMatchingStacks()) {
      if (iStack.getItemDamage() == OreDictionary.WILDCARD_VALUE) {
        NonNullList<ItemStack> subItems = NonNullList.create();
        iStack.getItem().getSubItems(CreativeTabs.SEARCH, subItems);

        for (ItemStack sStack : subItems) {
          BigItemStack bStack = new BigItemStack(sStack);
          bStack.stackSize = stack.stackSize;
          oreVariants.add(bStack);
        }
      } else {
        BigItemStack bStack = new BigItemStack(iStack);
        bStack.stackSize = stack.stackSize;
        oreVariants.add(bStack);
      }
    }
  }
}
