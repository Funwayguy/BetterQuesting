package betterquesting.questing.rewards;

import betterquesting.api.questing.IQuest;
import betterquesting.api.questing.rewards.IReward;
import betterquesting.api2.client.gui.misc.IGuiRect;
import betterquesting.api2.client.gui.panels.IGuiPanel;
import betterquesting.api2.storage.DBEntry;
import betterquesting.client.gui2.rewards.PanelRewardRecipe;
import betterquesting.questing.rewards.factory.FactoryRewardRecipe;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;

public class RewardRecipe implements IReward
{
    public String recipeNames = "minecraft:crafting_table\nminecraft:chest";
    
    @Override
    public String getUnlocalisedName()
    {
        return "bq_standard.reward.recipe";
    }
    
    @Override
    public ResourceLocation getFactoryID()
    {
        return FactoryRewardRecipe.INSTANCE.getRegistryName();
    }
    
    @Override
    public boolean canClaim(EntityPlayer player, DBEntry<IQuest> quest)
    {
        return true;
    }
    
    @Override
    public void claimReward(EntityPlayer player, DBEntry<IQuest> quest)
    {
        String[] recSplit = recipeNames.split("\n");
        ResourceLocation[] loc = new ResourceLocation[recSplit.length];
        
        for(int i = 0; i < recSplit.length; i++) loc[i] = new ResourceLocation(recSplit[i]);
        
        player.unlockRecipes(loc);
    }
    
    @Nullable
    @Override
    @SideOnly(Side.CLIENT)
    public IGuiPanel getRewardGui(IGuiRect rect, DBEntry<IQuest> quest)
    {
        return new PanelRewardRecipe(rect, this);
    }
    
    @Nullable
    @Override
    @SideOnly(Side.CLIENT)
    public GuiScreen getRewardEditor(GuiScreen parent, DBEntry<IQuest> quest)
    {
        return null;
    }
    
    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound nbt)
    {
        nbt.setString("recipes", recipeNames);
        return nbt;
    }
    
    @Override
    public void readFromNBT(NBTTagCompound nbt)
    {
        recipeNames = nbt.getString("recipes");
    }
}
