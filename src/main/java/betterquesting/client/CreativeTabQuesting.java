package betterquesting.client;

import betterquesting.core.BetterQuesting;
import javax.annotation.Nonnull;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;

public class CreativeTabQuesting extends CreativeTabs {
    public CreativeTabQuesting() {
        super(BetterQuesting.MODID);
    }

    @Nonnull
    @Override
    public Item getTabIconItem() {
        return BetterQuesting.extraLife;
    }
}
