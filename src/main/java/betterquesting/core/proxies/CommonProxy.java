package betterquesting.core.proxies;

import betterquesting.api.api.ApiReference;
import betterquesting.api.api.QuestingAPI;
import betterquesting.api.questing.rewards.IReward;
import betterquesting.api.questing.tasks.ITask;
import betterquesting.api2.registry.IFactoryData;
import betterquesting.api2.registry.IRegistry;
import betterquesting.core.BetterQuesting;
import betterquesting.core.ExpansionLoader;
import betterquesting.handlers.EventHandler;
import betterquesting.handlers.GuiHandler;
import betterquesting.network.handlers.*;
import betterquesting.questing.rewards.factory.*;
import betterquesting.questing.rewards.loot.LootRegistry;
import betterquesting.questing.tasks.factory.*;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.network.NetworkRegistry;

public class CommonProxy
{
	public boolean isClient()
	{
		return false;
	}
	
	public void registerHandlers()
	{
		ExpansionLoader.INSTANCE.initCommonAPIs();
		
		MinecraftForge.EVENT_BUS.register(EventHandler.INSTANCE);
        MinecraftForge.EVENT_BUS.register(new LootRegistry());
		MinecraftForge.TERRAIN_GEN_BUS.register(EventHandler.INSTANCE);
		
		NetworkRegistry.INSTANCE.registerGuiHandler(BetterQuesting.instance, new GuiHandler());
	}
	
	public void registerRenderers()
	{
	}

    public void registerExpansion()
    {
        IRegistry<IFactoryData<ITask, NBTTagCompound>, ITask> taskReg = QuestingAPI.getAPI(ApiReference.TASK_REG);
        taskReg.register(FactoryTaskBlockBreak.INSTANCE);
        taskReg.register(FactoryTaskCheckbox.INSTANCE);
        taskReg.register(FactoryTaskCrafting.INSTANCE);
        taskReg.register(FactoryTaskFluid.INSTANCE);
        taskReg.register(FactoryTaskHunt.INSTANCE);
        taskReg.register(FactoryTaskLocation.INSTANCE);
        taskReg.register(FactoryTaskMeeting.INSTANCE);
        taskReg.register(FactoryTaskRetrieval.INSTANCE);
        taskReg.register(FactoryTaskScoreboard.INSTANCE);
        taskReg.register(FactoryTaskXP.INSTANCE);
        taskReg.register(FactoryTaskAdvancement.INSTANCE);
        taskReg.register(FactoryTaskTame.INSTANCE);
        taskReg.register(FactoryTaskInteractItem.INSTANCE);
        taskReg.register(FactoryTaskInteractEntity.INSTANCE);
        taskReg.register(FactoryTaskTrigger.INSTANCE);

        IRegistry<IFactoryData<IReward, NBTTagCompound>, IReward> rewardReg = QuestingAPI.getAPI(ApiReference.REWARD_REG);
        rewardReg.register(FactoryRewardChoice.INSTANCE);
        rewardReg.register(FactoryRewardCommand.INSTANCE);
        rewardReg.register(FactoryRewardItem.INSTANCE);
        rewardReg.register(FactoryRewardScoreboard.INSTANCE);
        rewardReg.register(FactoryRewardXP.INSTANCE);
        rewardReg.register(FactoryRewardRecipe.INSTANCE);

        NetLootSync.registerHandler();
        NetLootClaim.registerHandler();
        NetTaskCheckbox.registerHandler();
        NetScoreSync.registerHandler();
        NetRewardChoice.registerHandler();
        NetLootImport.registerHandler();
        NetTaskInteract.registerHandler();

        BetterQuesting.lootChest.setCreativeTab(QuestingAPI.getAPI(ApiReference.CREATIVE_TAB));
    }
}
