package betterquesting.client.gui.editors.json;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.passive.EntityPig;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.logging.log4j.Level;
import betterquesting.api.client.gui.GuiScreenThemed;
import betterquesting.api.client.gui.controls.GuiButtonThemed;
import betterquesting.api.client.gui.lists.GuiScrollingButtons;
import betterquesting.api.misc.ICallback;
import betterquesting.api.utils.RenderUtils;
import betterquesting.core.BetterQuesting;

@SideOnly(Side.CLIENT)
public class GuiJsonEntitySelection extends GuiScreenThemed
{
	private Entity entity;
	private ICallback<Entity> callback;
	
	private GuiScrollingButtons btnList;
	
	public GuiJsonEntitySelection(GuiScreen parent, ICallback<Entity> callback, Entity entity)
	{
		super(parent, "betterquesting.title.select_entity");
		this.entity = entity;
		this.callback = callback;
		
		if(this.entity == null)
		{
			this.entity = new EntityPig(Minecraft.getMinecraft().theWorld);
		}
	}
	
	@Override
	public void initGui()
	{
		super.initGui();
		
		ArrayList<String> sortedNames = new ArrayList<String>();
		
		for(ResourceLocation loc : EntityList.getEntityNameList())
		{
			sortedNames.add(loc.toString());
		}
		
		Collections.sort(sortedNames);
		
		btnList = new GuiScrollingButtons(mc, guiLeft + sizeX/2, guiTop + 32, sizeX/2 - 16, sizeY - 64);
		int btnWidth = btnList.getListWidth();
		
		for(String key : sortedNames)
		{
			btnList.addButtonRow(new GuiButtonThemed(1, 0, 0, btnWidth, 20, key));
		}
		
		this.embedded.add(btnList);
	}
	
	@Override
	public void mouseClicked(int mx, int my, int click) throws IOException
	{
		super.mouseClicked(mx, my, click);
		
		GuiButtonThemed btn = btnList.getButtonUnderMouse(mx, my);
		
		if(btn != null && btn.mousePressed(mc, mx, my) && click == 0)
		{
			btn.playPressSound(mc.getSoundHandler());
			actionPerformed(btn);
		}
	}
	
	@Override
	public void actionPerformed(GuiButton button)
	{
		super.actionPerformed(button);
		
		if(button.id == 0 && callback != null)
		{
			callback.setValue(entity);
		} else if(button.id == 1)
		{
			Entity tmpE = EntityList.createEntityByIDFromName(new ResourceLocation(button.displayString), this.mc.theWorld);
			
			if(tmpE != null)
			{
				try
				{
					tmpE.readFromNBT(new NBTTagCompound()); // Solves some instantiation issues
					tmpE.isDead = false; // Some entities instantiate dead or die when ticked
					entity = tmpE;
				} catch(Exception e)
				{
					BetterQuesting.logger.log(Level.ERROR, "Failed to init selected entity", e);
				}
			}
		}
	}
	
	@Override
	public void drawScreen(int mx, int my, float partialTick)
	{
		super.drawScreen(mx, my, partialTick);
		
		if(entity != null)
		{
			GlStateManager.pushMatrix();
			
			GlStateManager.color(1F, 1F, 1F, 1F);
			
			float angle = ((float)Minecraft.getSystemTime()%30000F)/30000F * 360F;
			float scale = 64F;
			
			if(entity.height * scale > this.sizeY/2F)
			{
				scale = (this.sizeY/2F)/entity.height;
			}
			
			if(entity.width * scale > this.sizeX/4F)
			{
				scale = (this.sizeX/4F)/entity.width;
			}
			
			try
			{
				RenderUtils.RenderEntity(this.guiLeft + this.sizeX/4, this.guiTop + this.sizeY/2 + MathHelper.ceiling_float_int(entity.height/2F*scale), (int)scale, angle, 0F, entity);
			} catch(Exception e)
			{
			}
			
			GlStateManager.popMatrix();
		}
	}
}
