package adv_director.client.gui.editors.json;

import java.io.IOException;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.passive.EntityPig;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.logging.log4j.Level;
import adv_director.api.client.gui.GuiScreenThemed;
import adv_director.api.client.gui.controls.GuiBigTextField;
import adv_director.api.client.gui.controls.GuiButtonStorage;
import adv_director.api.client.gui.controls.GuiButtonThemed;
import adv_director.api.client.gui.lists.GuiScrollingButtons;
import adv_director.api.misc.ICallback;
import adv_director.api.utils.RenderUtils;
import adv_director.core.AdvDirector;

@SideOnly(Side.CLIENT)
public class GuiJsonEntitySelection extends GuiScreenThemed
{
	private Entity entity;
	private ICallback<Entity> callback;
	
	private final List<String> entityNames = new ArrayList<String>();
	
	private GuiBigTextField searchField;
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
		
		entityNames.addAll(EntityList.NAME_TO_CLASS.keySet());
		Collections.sort(entityNames);
		
		this.searchField = new GuiBigTextField(mc.fontRendererObj, guiLeft + sizeX/2 + 1, guiTop + 33, sizeX/2 - 18, 14);
		this.searchField.setWatermark(I18n.format("betterquesting.gui.search"));
		this.searchField.setMaxStringLength(Integer.MAX_VALUE);
		
		btnList = new GuiScrollingButtons(mc, guiLeft + sizeX/2, guiTop + 48, sizeX/2 - 16, sizeY - 80);
		this.embedded.add(btnList);
		
		this.searching = entityNames.iterator();
		this.updateSearch();
	}
	
	@Override
	public void mouseClicked(int mx, int my, int click) throws IOException
	{
		super.mouseClicked(mx, my, click);
		
		this.searchField.mouseClicked(mx, my, click);
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
		if(button.id == 0 && callback != null)
		{
			callback.setValue(entity);
		} else if(button.id == 1)
		{
			@SuppressWarnings("unchecked")
			Entity tmpE = EntityList.createEntityByName(((GuiButtonStorage<String>)button).getStored(), this.mc.theWorld);
			
			if(tmpE != null)
			{
				try
				{
					tmpE.readFromNBT(new NBTTagCompound()); // Solves some instantiation issues
					tmpE.isDead = false; // Some entities instantiate dead or die when ticked
					entity = tmpE;
				} catch(Exception e)
				{
					AdvDirector.logger.log(Level.ERROR, "Failed to init selected entity", e);
				}
			}
		}
		
		super.actionPerformed(button);
	}
	
	@Override
	public void drawScreen(int mx, int my, float partialTick)
	{
		this.updateSearch();
		
		super.drawScreen(mx, my, partialTick);
		
		this.searchField.drawTextBox(mx, my, partialTick);
		
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
	
	private String searchTxt = "";
	private Iterator<String> searching = null;
	
	private void updateSearch()
	{
		if(searching == null)
		{
			return;
		} else if(!searching.hasNext())
		{
			searching = null;
			return;
		}
		
		int pass = 0;
		int btnWidth = btnList.getListWidth();
		
		while(searching.hasNext() && pass < 64)
		{
			String key = searching.next();
			
			boolean abs = Modifier.isAbstract(EntityList.NAME_TO_CLASS.get(key).getModifiers());
			
			if(!abs && key.toLowerCase().contains(searchTxt))
			{
				GuiButtonStorage<String> btn = new GuiButtonStorage<String>(1, 0, 0, btnWidth, 20, key);
				btn.setStored(key);
				btnList.addButtonRow(btn);
			}
		}
	}
	
	@Override
	public void keyTyped(char c, int keyCode) throws IOException
	{
		super.keyTyped(c, keyCode);
		
		searchField.textboxKeyTyped(c, keyCode);
		
		if(!searchField.getText().equalsIgnoreCase(searchTxt))
		{
			btnList.getEntryList().clear();
			searchTxt = searchField.getText().toLowerCase();
			searching = entityNames.iterator();
		}
	}
}
