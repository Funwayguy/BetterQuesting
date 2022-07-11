package betterquesting.items;

import betterquesting.api.storage.BQ_Settings;
import betterquesting.client.gui2.GuiHome;
import betterquesting.core.BetterQuesting;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;

import javax.annotation.Nonnull;

public class ItemQuestBook extends Item {

    public ItemQuestBook() {

        this.setTranslationKey("betterquesting.quest_book");
        this.setCreativeTab(BetterQuesting.tabQuesting);
    }

    @Nonnull
    @Override
    public ActionResult<ItemStack> onItemRightClick(@Nonnull World world, @Nonnull EntityPlayer player, @Nonnull EnumHand hand) {

        ItemStack stack = player.getHeldItem(hand);

        if(world.isRemote) {
            if(stack.getItem() == BetterQuesting.questBook) {
                Minecraft mc = Minecraft.getMinecraft();
                if(BQ_Settings.useBookmark && GuiHome.bookmark != null) {
                    mc.displayGuiScreen(GuiHome.bookmark);
                }
                else {
                    mc.displayGuiScreen(new GuiHome(mc.currentScreen));
                }
            }
        }

        return new ActionResult<>(EnumActionResult.PASS, stack);
    }
}
