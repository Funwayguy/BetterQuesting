package betterquesting.items;

import betterquesting.core.BetterQuesting;
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

        if(world.isRemote && stack.getItem() == BetterQuesting.questBook) {
            player.openGui(BetterQuesting.instance, 3, world, player.getPosition().getX(), player.getPosition().getY(), player.getPosition().getZ());
        }

        return new ActionResult<>(EnumActionResult.PASS, stack);
    }
}
