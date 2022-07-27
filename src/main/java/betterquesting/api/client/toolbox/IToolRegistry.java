package betterquesting.api.client.toolbox;

import betterquesting.api2.client.toolbox.IToolTab;
import java.util.Collection;
import net.minecraft.util.ResourceLocation;

public interface IToolRegistry {
    void registerToolTab(ResourceLocation tabID, IToolTab tab);

    IToolTab getTabByID(ResourceLocation tabID);

    Collection<IToolTab> getAllTabs();
}
