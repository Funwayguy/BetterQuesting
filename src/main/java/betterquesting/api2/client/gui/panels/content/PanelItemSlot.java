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
import java.util.*;
import net.minecraft.client.Minecraft;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumChatFormatting;
import net.minecraftforge.oredict.OreDictionary;

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

        this.setTextures(
                PresetTexture.ITEM_FRAME.getTexture(),
                PresetTexture.ITEM_FRAME.getTexture(),
                new LayeredTexture(
                        PresetTexture.ITEM_FRAME.getTexture(),
                        new ColorTexture(PresetColor.ITEM_HIGHLIGHT.getColor(), new GuiPadding(1, 1, 1, 1))));
        this.setStoredValue(value); // Need to run this again because of the instatiation order of showCount
    }

    @Override
    public PanelItemSlot setStoredValue(BigItemStack value) {
        super.setStoredValue(value);

        if (value != null) {
            Minecraft mc = Minecraft.getMinecraft();
            this.setIcon(
                    oreDict || value.getBaseStack().getItemDamage() == OreDictionary.WILDCARD_VALUE
                            ? new OreDictTexture(1F, value, showCount, true)
                            : new ItemTexture(value, showCount, true),
                    1);
            try {
                this.setTooltip(value.getBaseStack().getTooltip(mc.thePlayer, mc.gameSettings.advancedItemTooltips));
            } catch (NullPointerException ignored) {
                this.setTooltip(Collections.singletonList("" + EnumChatFormatting.RED + EnumChatFormatting.BOLD
                        + EnumChatFormatting.ITALIC + "Broken tooltip. Please REPORT TO DEV."));
            }
        } else {
            this.setIcon(null);
            this.setTooltip(null);
        }

        updateOreStacks();

        return this;
    }

    @Override
    public List<String> getTooltip(int mx, int my) {
        BigItemStack ttStack = getStoredValue();
        if (ttStack == null || !getTransform().contains(mx, my)) return null;

        if (oreDict && oreVariants.size() > 0) {
            ttStack = oreVariants.get((int) (System.currentTimeMillis() / 1000D) % oreVariants.size());
        }

        if (ttStack != null) {
            Minecraft mc = Minecraft.getMinecraft();
            return ttStack.getBaseStack().getTooltip(mc.thePlayer, mc.gameSettings.advancedItemTooltips);
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
        if (stack == null) return;

        if (!stack.hasOreDict()) {
            if (stack.getBaseStack().getItemDamage() == OreDictionary.WILDCARD_VALUE) {
                List<ItemStack> subItems = new ArrayList<>();
                stack.getBaseStack()
                        .getItem()
                        .getSubItems(stack.getBaseStack().getItem(), CreativeTabs.tabAllSearch, subItems);

                for (ItemStack sStack : subItems) {
                    BigItemStack bStack = new BigItemStack(sStack);
                    bStack.stackSize = stack.stackSize;
                    oreVariants.add(bStack);
                }
            } else {
                oreVariants.add(stack);
            }
            return;
        }

        for (ItemStack iStack : stack.getOreIngredient().getMatchingStacks()) {
            if (iStack.getItemDamage() == OreDictionary.WILDCARD_VALUE) {
                List<ItemStack> subItems = new ArrayList<>();
                iStack.getItem().getSubItems(iStack.getItem(), CreativeTabs.tabAllSearch, subItems);

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
