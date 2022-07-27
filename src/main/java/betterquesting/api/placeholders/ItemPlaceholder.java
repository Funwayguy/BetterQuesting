package betterquesting.api.placeholders;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import java.util.List;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;

public class ItemPlaceholder extends Item {
    public static Item placeholder = new ItemPlaceholder();

    // Used solely for retaining info to missing items
    public ItemPlaceholder() {
        this.setTextureName("betterquesting:placeholder");
        this.setUnlocalizedName("betterquesting.placeholder");
    }

    /**
     * allows items to add custom lines of information to the mouseover description
     */
    @Override
    @SideOnly(Side.CLIENT)
    @SuppressWarnings("unchecked")
    public void addInformation(ItemStack stack, EntityPlayer player, List tooltip, boolean advanced) {
        if (!stack.hasTagCompound()) {
            tooltip.add("ERROR: Original information missing!");
            return;
        }

        tooltip.add("Original ID: " + stack.getTagCompound().getString("orig_id") + "/"
                + stack.getTagCompound().getInteger("orig_meta"));
    }

    /**
     * Called each tick as long the item is on a player inventory. Uses by maps to check if is on a player hand and
     * update it's contents.
     */
    @Override
    public void onUpdate(ItemStack stack, World world, Entity entity, int slot, boolean held) {
        if (!stack.hasTagCompound()
                || !(entity instanceof EntityPlayer)
                || world.getTotalWorldTime() % 100 != 0) // Process this only once a second
        {
            return;
        }

        EntityPlayer player = (EntityPlayer) entity;

        NBTTagCompound tags = stack.getTagCompound();
        Item i = (Item) Item.itemRegistry.getObject(tags.getString("orig_id"));
        int m = stack.getItemDamage() > 0 ? stack.getItemDamage() : tags.getInteger("orig_meta");
        NBTTagCompound t = tags.hasKey("orig_tag") ? tags.getCompoundTag("orig_tag") : null;

        if (i != null) {
            ItemStack converted = new ItemStack(i, stack.stackSize, m);
            converted.setTagCompound(t);
            player.inventory.setInventorySlotContents(slot, converted);
        }
    }
}
