package betterquesting.client.themes;

import java.awt.Color;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ResourceLocation;
import betterquesting.api.client.themes.IThemeBase;
import betterquesting.api.enums.EnumQuestState;
import betterquesting.api.quests.IQuest;
import betterquesting.api.quests.properties.NativeProps;

public class ThemeStandard implements IThemeBase
{
	private final ResourceLocation regName;
	private final String name;
	private final ResourceLocation guiTexture;
	private ResourceLocation btnSound = new ResourceLocation("gui.button.press");
	private int txtColor = Color.BLACK.getRGB();
	private int[] lineColors = new int[]{new Color(0.75F, 0F, 0F).getRGB(), Color.YELLOW.getRGB(), Color.GREEN.getRGB(), Color.GREEN.getRGB()};
	private int[] iconColors = new int[]{Color.GRAY.getRGB(), new Color(0.75F, 0F, 0F).getRGB(), new Color(0F, 1F, 1F).getRGB(), Color.GREEN.getRGB()};
	
	public ThemeStandard(String name, ResourceLocation texture, ResourceLocation regName)
	{
		this.regName = regName;
		this.name = name;
		this.guiTexture = texture;
	}
	
	@Override
	public ResourceLocation getThemeID()
	{
		return regName;
	}
	
	@Override
	public String getDisplayName()
	{
		return name;
	}
	
	@Override
	public ResourceLocation getGuiTexture()
	{
		return guiTexture;
	}
	
	public ThemeStandard setTextColor(int c)
	{
		txtColor = c;
		return this;
	}
	
	public ThemeStandard setLineColors(int locked, int incomplete, int complete)
	{
		lineColors[0] = locked;
		lineColors[1] = incomplete;
		lineColors[2] = complete;
		lineColors[3] = complete;
		return this;
	}
	
	public ThemeStandard setIconColors(int locked, int incomplete, int pending, int complete)
	{
		iconColors[0] = locked;
		iconColors[1] = incomplete;
		iconColors[2] = pending;
		iconColors[3] = complete;
		return this;
	}
	
	public ThemeStandard setButtonSound(ResourceLocation sound)
	{
		this.btnSound = sound;
		return this;
	}
	
	@Override
	public int getTextColor()
	{
		return txtColor;
	}
	
	@Override
	public short getLineStipple(IQuest quest, EnumQuestState state)
	{
		return (short)0xFFFF;
	}
	
	@Override
	public float getLineWidth(IQuest quest, EnumQuestState state)
	{
		return quest.getProperties().getProperty(NativeProps.MAIN)? 8F : 4F;
	}
	
	@Override
	public int getQuestLineColor(IQuest quest, EnumQuestState state)
	{
		Color c = new Color(lineColors[state.ordinal()]);
		
		if(state == EnumQuestState.UNLOCKED && (Minecraft.getSystemTime()/1000)%2 == 0)
		{
			return new Color(c.getRed()/255F*0.5F, c.getGreen()/255F*0.5F, c.getBlue()/255F*0.5F).getRGB();
		}
		
		return c.getRGB();
	}
	
	@Override
	public int getQuestIconColor(IQuest quest, EnumQuestState state, int hoverState)
	{
		Color c = new Color(iconColors[state.ordinal()]);
		
		if(hoverState == 1)
		{
			return new Color(c.getRed()/255F*0.75F, c.getGreen()/255F*0.75F, c.getBlue()/255F*0.75F).getRGB();
		}
		
		return c.getRGB();
	}

	@Override
	public ResourceLocation getButtonSound()
	{
		return btnSound;
	}
}
