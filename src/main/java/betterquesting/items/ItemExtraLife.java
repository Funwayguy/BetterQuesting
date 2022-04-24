package betterquesting.items;

import betterquesting.api.api.QuestingAPI;
import betterquesting.api.properties.NativeProps;
import betterquesting.core.BetterQuesting;
import betterquesting.storage.LifeDatabase;
import betterquesting.storage.QuestSettings;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.*;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;
import java.util.UUID;

public class ItemExtraLife extends Item {
    public ItemExtraLife() {
        this.setTranslationKey("betterquesting.extra_life");
        this.setCreativeTab(BetterQuesting.tabQuesting);
        this.setHasSubtypes(true);
    }

    /**
     * Called whenever this item is equipped and the right mouse button is pressed. Args: itemStack, world, entityPlayer
     */
    @Nonnull
    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, @Nonnull EnumHand hand) {
        ItemStack stack = player.getHeldItem(hand);

        if (stack.getItemDamage() != 0 || hand != EnumHand.MAIN_HAND) {
            return new ActionResult<>(EnumActionResult.PASS, stack);
        } else if (QuestSettings.INSTANCE.getProperty(NativeProps.HARDCORE)) {
            if (!player.capabilities.isCreativeMode) {
                stack.grow(-1);
            }

            UUID uuid = QuestingAPI.getQuestingUUID(player);
            int lives = LifeDatabase.INSTANCE.getLives(uuid);

            if (lives >= QuestSettings.INSTANCE.getProperty(NativeProps.LIVES_MAX)) {
                if (!world.isRemote) {
                    player.sendStatusMessage(new TextComponentString(TextFormatting.RED.toString()).appendSibling(new TextComponentTranslation("betterquesting.gui.full_lives")), true);
                }

                return new ActionResult<>(EnumActionResult.PASS, stack);
            }

            player.world.playSound(null, player.posX, player.posY, player.posZ, SoundEvents.ENTITY_PLAYER_LEVELUP, SoundCategory.PLAYERS, 1F, 1F);

            if (!world.isRemote) {
                LifeDatabase.INSTANCE.setLives(uuid, lives + 1);

                player.sendStatusMessage(new TextComponentTranslation("betterquesting.gui.remaining_lives", TextFormatting.YELLOW.toString() + (lives + 1)), true);
            }
        } else if (!world.isRemote) {
            player.sendStatusMessage(new TextComponentTranslation("betterquesting.msg.heart_disabled"), true);
        }

        return new ActionResult<>(EnumActionResult.PASS, stack);
    }

    /**
     * Returns the unlocalized name of this item. This version accepts an ItemStack so different stacks can have
     * different names based on their damage or NBT.
     */
    @Override
    @Nonnull
    public String getTranslationKey(ItemStack stack) {
        switch (stack.getItemDamage() % 3) {
            case 2:
                return this.getTranslationKey() + ".quarter";
            case 1:
                return this.getTranslationKey() + ".half";
            default:
                return this.getTranslationKey() + ".full";
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public boolean hasEffect(ItemStack stack) {
        return stack.getItemDamage() == 0;
    }

    /**
     * returns a list of items with the same ID, but different meta (eg: dye returns 16 items)
     */
    @Override
    @SideOnly(Side.CLIENT)
    public void getSubItems(@Nonnull CreativeTabs tab, @Nonnull NonNullList<ItemStack> list) {
        if (this.isInCreativeTab(tab)) {
            list.add(new ItemStack(this, 1, 0));
            list.add(new ItemStack(this, 1, 1));
            list.add(new ItemStack(this, 1, 2));
        }
    }
}
