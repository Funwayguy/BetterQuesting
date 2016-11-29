package betterquesting.client.gui.party;

import java.util.List;
import java.util.UUID;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.resources.I18n;
import betterquesting.api.client.gui.GuiElement;
import betterquesting.api.client.gui.controls.GuiButtonThemed;
import betterquesting.api.client.gui.lists.GuiScrollingBase;
import betterquesting.api.client.gui.lists.IScrollingEntry;
import betterquesting.api.utils.RenderUtils;
import betterquesting.storage.NameCache;

public class GuiScrollingParty extends GuiScrollingBase<GuiScrollingParty.ScrollingEntryParty>
{
	public GuiScrollingParty(Minecraft mc, int x, int y, int w, int h)
	{
		super(mc, x, y, w, h);
	}
	
	public void addUser(UUID uuid)
	{
		this.getEntryList().add(new ScrollingEntryParty(uuid, this.getListWidth()));
	}
	
	public UUID checkKickRequest(int mx, int my)
	{
		return null;
	}
	
	public class ScrollingEntryParty extends GuiElement implements IScrollingEntry
	{
		private final GuiButtonThemed btnKick;
		private final String name;
		
		public ScrollingEntryParty(UUID uuid, int width)
		{
			this.name = NameCache.INSTANCE.getName(uuid);
			this.btnKick = new GuiButtonThemed(0, 0, 0, 50, 20, I18n.format("betterquesting.btn.party_kick"));
		}
		
		@Override
		public void drawBackground(int mx, int my, int px, int py, int width)
		{
			FontRenderer fr = btnKick.mc.fontRendererObj;
			this.drawString(fr, fr.trimStringToWidth(name, width - 50), px, py + 6, getTextColor(), false);
			
			RenderUtils.DrawLine(px, py + 20, px + width - btnKick.width, py + 20, 1F, getTextColor());
			
			btnKick.xPosition = px + width - btnKick.width;
			btnKick.yPosition = py;
			btnKick.drawButton(btnKick.mc, mx, my);
			
			// new EntityOtherPlayerMP(mc.theWorld, new GameProfile(null, name)); // < Draw player?
		}

		@Override
		public void drawForeground(int mx, int my, int px, int py, int width)
		{
			if(isWithin(mx, my, btnKick.xPosition, btnKick.yPosition, btnKick.width, btnKick.height))
			{
				List<String> tt = btnKick.getTooltip();
				
				if(tt != null && tt.size() > 0)
				{
					this.drawTooltip(btnKick.getTooltip(), mx, my, btnKick.mc.fontRendererObj);
				}
			}
		}

		@Override
		public void onMouseClick(int mx, int my, int px, int py, int click, int index)
		{
		}

		@Override
		public int getHeight()
		{
			return 20;
		}

		@Override
		public boolean canDrawOutsideBox(boolean isForeground)
		{
			return false;
		}
		
	}
}
