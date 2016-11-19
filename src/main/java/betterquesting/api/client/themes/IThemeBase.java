package betterquesting.api.client.themes;

import javax.annotation.Nullable;
import net.minecraft.util.ResourceLocation;
import betterquesting.api.enums.EnumQuestState;
import betterquesting.api.quests.IQuest;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public interface IThemeBase
{
	public ResourceLocation getThemeID();
	public String getDisplayName();
	
	public ResourceLocation getGuiTexture();
	
	public int getQuestIconColor(@Nullable IQuest quest, EnumQuestState state, int hoverState);
	public int getQuestLineColor(@Nullable IQuest quest, EnumQuestState state);
	public int getTextColor();
	
	@Deprecated
	public short getLineStipple(@Nullable IQuest quest, EnumQuestState state);
	@Deprecated
	public float getLineWidth(@Nullable IQuest quest, EnumQuestState state);
	@Deprecated
	public ResourceLocation getButtonSound();
	
	// COMING SOON
	//public IThemeRenderer getRenderer();
	//public ResourceLocation getButtonSound(GuiButton button);
}