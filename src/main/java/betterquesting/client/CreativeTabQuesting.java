package betterquesting.client;

import betterquesting.core.BetterQuesting;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nonnull;

public class CreativeTabQuesting extends ItemGroup
{
	private ItemStack tabStack;
	
	public CreativeTabQuesting()
	{
		super(BetterQuesting.MODID);
	}
	
	@Nonnull
	@Override
    @OnlyIn(Dist.CLIENT)
	public ItemStack createIcon()
	{
		if(tabStack == null) this.tabStack = new ItemStack(Items.WRITTEN_BOOK/*BetterQuesting.extraLife*/);
		return tabStack;
	}
}
