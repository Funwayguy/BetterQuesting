package betterquesting.api.client.toolbox;

import betterquesting.api2.client.toolbox.IToolTab;
import net.minecraft.util.ResourceLocation;

import java.util.Collection;

public interface IToolRegistry {
    void registerToolTab(ResourceLocation tabID, IToolTab tab);

    IToolTab getTabByID(ResourceLocation tabID);

    Collection<IToolTab> getAllTabs();
}
