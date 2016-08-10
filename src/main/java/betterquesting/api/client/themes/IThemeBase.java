package betterquesting.api.client.themes;

import javax.annotation.Nullable;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.util.ResourceLocation;
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
	
	public short getLineStipple(@Nullable IQuestContainer quest, EnumQuestState state);
	public float getLineWidth(@Nullable IQuestContainer quest, EnumQuestState state);
	
	public ResourceLocation getButtonSound();
	
	public GuiScreen getGuiOverride(GuiScreen gui);
}