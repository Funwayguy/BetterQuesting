package betterquesting.client;

import betterquesting.core.BetterQuesting;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;

import javax.annotation.Nonnull;

public class CreativeTabQuesting extends CreativeTabs {
  private ItemStack tabStack;

  public CreativeTabQuesting() {
    super(BetterQuesting.MODID);
  }

  @Nonnull
  @Override
  public ItemStack createIcon() {
    if (tabStack == null) {
      this.tabStack = new ItemStack(BetterQuesting.extraLife);
    }

    return tabStack;
  }
}
