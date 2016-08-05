package betterquesting.api.client.themes;

import javax.annotation.Nullable;
import net.minecraft.util.ResourceLocation;
import betterquesting.api.client.gui.IGuiEmbedded;
import betterquesting.api.enums.EnumQuestState;
import betterquesting.api.quests.IQuestContainer;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public interface IThemeBase
{
	public ResourceLocation getThemeID();
	public String getDisplayName();
	
	public ResourceLocation getGuiTexture();
	
	public int getQuestIconColor(@Nullable IQuestContainer quest, EnumQuestState state, int hoverState);
	public int getQuestLineColor(@Nullable IQuestContainer quest, EnumQuestState state);
	public int getTextColor();
	
	public int getLineStipple(@Nullable IQuestContainer quest, EnumQuestState state);
	public float getLineWidth(@Nullable IQuestContainer quest, EnumQuestState state);
	
	public ResourceLocation getButtonSound();
	
	public IGuiEmbedded getGuiOverride(IGuiEmbedded gui);
}