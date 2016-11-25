package betterquesting.client.gui.editors.json;

import java.util.ArrayList;
import java.util.Iterator;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.IIcon;
import net.minecraft.util.MathHelper;
import net.minecraft.util.StatCollector;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidContainerRegistry;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import org.lwjgl.opengl.GL11;
import betterquesting.api.client.gui.GuiScreenThemed;
import betterquesting.api.client.gui.controls.GuiBigTextField;
import betterquesting.api.client.gui.controls.GuiButtonThemed;
import betterquesting.api.client.gui.controls.GuiNumberField;
import betterquesting.api.misc.ICallback;
import betterquesting.api.utils.RenderUtils;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class GuiJsonFluidSelection extends GuiScreenThemed
{
	private FluidStack stackSelect;
	private ICallback<FluidStack> callback;
	
	private GuiBigTextField searchBox;
	private GuiNumberField numberBox;
	private ArrayList<FluidStack> searchResults = new ArrayList<FluidStack>();
	private int searchPage = 0;
	private int rows = 1;
	private int columns = 1;
	
	public GuiJsonFluidSelection(GuiScreen parent, ICallback<FluidStack> callback, FluidStack stack)
	{
		super(parent, "betterquesting.title.select_fluid");
		this.stackSelect = stack;
		this.callback = callback;
		
		if(stackSelect == null)
		{
			stackSelect = new FluidStack(FluidRegistry.WATER, 1000);
		}
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public void initGui()
	{
		super.initGui();
		
		int srcW = sizeX/2 - 34 - (sizeX/2 - 32)%18;
		this.searchBox = new GuiBigTextField(fontRendererObj, guiLeft + sizeX/2 + 9, guiTop + 33, srcW, 14);
		this.searchBox.setWatermark(I18n.format("betterquesting.gui.search"));
		this.searchBox.setMaxStringLength(Integer.MAX_VALUE);
		
		numberBox = new GuiNumberField(fontRendererObj, guiLeft + 76, guiTop + 57, 100, 16);
		
		searchResults.clear();
		searching = FluidRegistry.getRegisteredFluids().values().iterator();
		
		columns = (sizeX/2 - 32)/18;
		rows = (sizeY - (48 + 48))/18;
		
		GuiButtonThemed leftBtn = new GuiButtonThemed(1, this.guiLeft + this.sizeX/2 + 8, this.guiTop + this.sizeY - 48, 20, 20, "<", true);
		this.buttonList.add(leftBtn);
		GuiButtonThemed rightBtn = new GuiButtonThemed(2, this.guiLeft + this.sizeX/2 + 8 + columns*18 - 20, this.guiTop + this.sizeY - 48, 20, 20, ">", true);
		this.buttonList.add(rightBtn);
	}
	
	@Override
	public void drawScreen(int mx, int my, float partialTick)
	{
		super.drawScreen(mx, my, partialTick);
		
		doSearch();
		
		FluidStack ttStack = null;
		int btnWidth = sizeX/2 - 16;
		
		GL11.glColor4f(1f, 1f, 1f, 1f);
		
		this.fontRendererObj.drawString(I18n.format("betterquesting.gui.selection"), guiLeft + 24, guiTop + 36, getTextColor(), false);
		this.fontRendererObj.drawString("x", guiLeft + 64, guiTop + 60, getTextColor(), false);
		
		this.mc.renderEngine.bindTexture(currentTheme().getGuiTexture());
		
		GL11.glPushMatrix();
		GL11.glScalef(2F, 2F, 1F);
		GL11.glColor4f(1F, 1F, 1F, 1F);
		this.drawTexturedModalRect((guiLeft + 24)/2, (guiTop + 48)/2, 0, 48, 18, 18);
		
		if(this.stackSelect != null)
		{
			mc.renderEngine.bindTexture(TextureMap.locationBlocksTexture);
			GL11.glColor4f(1F, 1F, 1F, 1F);
			
			try
			{
				if(stackSelect.getFluid().getIcon() != null)
				{
					RenderUtils.itemRender.renderIcon((guiLeft + 26)/2, (guiTop + 50)/2, stackSelect.getFluid().getIcon(), 16, 16);
				} else
				{
		            IIcon missing = ((TextureMap)mc.renderEngine.getTexture(TextureMap.locationBlocksTexture)).getAtlasSprite("missingno");
					RenderUtils.itemRender.renderIcon((guiLeft + 26)/2, (guiTop + 50)/2, missing, 16, 16);
				}
			} catch(Exception e){}
			
			if(this.isWithin(mx, my, 25, 49, 32, 32))
			{
				ttStack = this.stackSelect;
			}
		}
		GL11.glPopMatrix();
		
		fontRendererObj.drawString(I18n.format("container.inventory"), this.guiLeft + 24, this.guiTop + sizeY/2 - 12, getTextColor(), false);
		
		GL11.glColor4f(1F, 1F, 1F, 1F);
		
		if(this.mc.thePlayer != null)
		{
			ItemStack[] invoStacks = this.mc.thePlayer.inventory.mainInventory;
			
			int isx = (18 * 9);
			int isy = (18 * 4);
			float scale = Math.min((btnWidth - 16)/(float)isx, (sizeY/2F - 32)/isy);
			int ipx = guiLeft + 16 + btnWidth/2 - (int)(isx/2*scale);
			int ipy = guiTop + sizeY/2;
			
			GL11.glPushMatrix();
			GL11.glTranslatef(ipx, ipy, 0F);
			GL11.glScalef(scale, scale, 1F);
			for(int i = 0; i < invoStacks.length && i < 9 * 4; i++)
			{
				int x = i%9 * 18;
				int y = (i - i%9)/9 * 18;
				
				this.mc.renderEngine.bindTexture(currentTheme().getGuiTexture());
				GL11.glDisable(GL11.GL_DEPTH_TEST);
				this.drawTexturedModalRect(x, y, 0, 48, 18, 18);
				GL11.glEnable(GL11.GL_DEPTH_TEST);
				
				ItemStack stack = invoStacks[i];
				
				if(stack != null)
				{
					RenderUtils.RenderItemStack(mc, stack, x + 1, y + 1, "" + (stack.stackSize > 1? stack.stackSize : ""));
					
					if(isWithin(mx, my, ipx + (int)((x + 1)*scale), ipy + (int)((y + 1)*scale), (int)(16*scale), (int)(16*scale), false) && FluidContainerRegistry.isFilledContainer(stack))
					{
						ttStack = FluidContainerRegistry.getFluidForFilledItem(stack);
					}
				}
			}
			GL11.glPopMatrix();
		}
		
		RenderUtils.DrawLine(width/2, guiTop + 32, width/2, guiTop + sizeY - 32, 2F, getTextColor());
		
		int mxPage = Math.max(MathHelper.ceiling_float_int(searchResults.size()/(float)(columns * rows)), 1);
		this.fontRendererObj.drawString((searchPage + 1) + "/" + mxPage, guiLeft + 16 + (sizeX - 32)/4*3, guiTop + sizeY - 42, getTextColor(), false);
		
		this.searchBox.drawTextBox(mx, my, partialTick);
		this.numberBox.drawTextBox();
		
		GL11.glColor4f(1f, 1f, 1f, 1f);
		
		int x = 0;
		int y = 0;
		
		for(int i = (columns * rows * searchPage); i < searchResults.size(); i++)
		{
			int n = i - (columns * rows * searchPage);
			x = n%columns * 18;
			y = (n - n%columns)/columns * 18;
			
			if(y > this.sizeY - (48 + 48 + 18))
			{
				break;
			}
			
			this.mc.renderEngine.bindTexture(currentTheme().getGuiTexture());
			
			FluidStack resultStack = searchResults.get(i);
			
			if(resultStack != null)
			{
				GL11.glDisable(GL11.GL_DEPTH_TEST);
				this.drawTexturedModalRect(guiLeft + sizeX/2 + x + 8, guiTop + 48 + y, 0, 48, 18, 18);
				GL11.glEnable(GL11.GL_DEPTH_TEST);
				
				mc.renderEngine.bindTexture(TextureMap.locationBlocksTexture);
				GL11.glColor4f(1F, 1F, 1F, 1F);
				
				try
				{
					if(resultStack.getFluid().getIcon() != null)
					{
						RenderUtils.itemRender.renderIcon(guiLeft + sizeX/2 + 9 + x, guiTop + 49 + y, resultStack.getFluid().getIcon(), 16, 16);
					} else
					{
			            IIcon missing = ((TextureMap)mc.renderEngine.getTexture(TextureMap.locationBlocksTexture)).getAtlasSprite("missingno");
						RenderUtils.itemRender.renderIcon(guiLeft + sizeX/2 + 9 + x, guiTop + 49 + y, missing, 16, 16);
					}
				} catch(Exception e){}
				
				if(this.isWithin(mx, my, this.sizeX/2 + x + 9, 49 + y, 16, 16))
				{
					ttStack = resultStack;
				}
			}
		}
		
		if(ttStack != null)
		{
			ArrayList<String> tTip = new ArrayList<String>();
			tTip.add(ttStack.getLocalizedName());
			tTip.add(EnumChatFormatting.GRAY + "" + ttStack.amount + " mB");
			this.drawTooltip(tTip, mx, my);
		}
	}
	
	@Override
	public void actionPerformed(GuiButton button)
	{
		super.actionPerformed(button);
		
		if(button.id == 0 && callback != null)
		{
			callback.setValue(stackSelect);
		} else if(button.id == 1 && searchPage > 0)
		{
			searchPage--;
		} else if(button.id == 2)
		{
			if(columns * rows * (searchPage + 1) < searchResults.size())
			{
				searchPage++;
			}
		}
	}
	
	@Override
	public void mouseClicked(int mx, int my, int type)
	{
		super.mouseClicked(mx, my, type);
		this.searchBox.mouseClicked(mx, my, type);
		this.numberBox.mouseClicked(mx, my, type);
		
		int btnWidth = sizeX/2 - 16;
		
		int isx = (18 * 9);
		int isy = (18 * 4);
		float scale = Math.min((btnWidth - 16)/(float)isx, (sizeY/2F - 32)/isy);
		int ipx = guiLeft + 16 + btnWidth/2 - (int)(isx/2*scale);
		int ipy = guiTop + sizeY/2;
		
		if(this.mc.thePlayer != null && this.isWithin(mx, my, ipx, ipy, (int)(18 * 9 * scale), (int)(18 * 4 * scale), false))
		{
			int idxSize = (int)(18*scale);
			int sx = (mx - ipx)/idxSize;
			int sy = (my - ipy)/idxSize;
			int index = sx + (sy * 9);
			
			if(index >= 0 && index < this.mc.thePlayer.inventory.mainInventory.length)
			{
				ItemStack invoStack = this.mc.thePlayer.inventory.mainInventory[index];
				
				if(invoStack != null && FluidContainerRegistry.isFilledContainer(invoStack))
				{
					this.stackSelect = FluidContainerRegistry.getFluidForFilledItem(invoStack).copy();
					numberBox.setText("" + stackSelect.amount);
				}
			}
		} else if(this.isWithin(mx, my, this.sizeX/2, 48, columns * 18, rows * 18))
		{

			int sx = (mx - (this.guiLeft + this.sizeX/2 + 8))/18;
			int sy = (my - (this.guiTop + 48))/18;
			int index = sx + (sy * columns) + (searchPage * columns * rows);
			
			if(index >= 0 && index < this.searchResults.size())
			{
				FluidStack searchFluid = this.searchResults.get(index);
				
				if(searchFluid != null)
				{
					this.stackSelect = searchFluid.copy();
					numberBox.setText("" + stackSelect.amount);
				}
			}
		} else if(!numberBox.isFocused() && stackSelect != null && stackSelect.amount != numberBox.getNumber().intValue())
		{
			int i = Math.max(1, numberBox.getNumber().intValue());
			numberBox.setText("" + i);
			stackSelect.amount = i;
		}
	}
	
	Iterator<Fluid> searching = null;
	String searchTxt = "";
	
	public void doSearch()
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
		
		while(searching.hasNext() && pass < 256)
		{
			Fluid baseFluid = searching.next();
			
			if(baseFluid == null)
			{
				continue;
			}
			
			pass++;
			
			if(baseFluid.getUnlocalizedName() == null || FluidRegistry.getDefaultFluidName(baseFluid) == null)
			{
				continue;
			}
			
			if(baseFluid.getUnlocalizedName().toLowerCase().contains(searchTxt) || StatCollector.translateToLocal(baseFluid.getUnlocalizedName()).toLowerCase().contains(searchTxt) || FluidRegistry.getDefaultFluidName(baseFluid).toLowerCase().contains(searchTxt))
			{
				searchResults.add(new FluidStack(baseFluid, 1000));
			}
		}
	}
	
    /**
     * Fired when a key is typed. This is the equivalent of KeyListener.keyTyped(KeyEvent e).
     */
	@Override
    protected void keyTyped(char character, int num)
    {
		super.keyTyped(character, num);
		String prevTxt = searchBox.getText();
		
		searchBox.textboxKeyTyped(character, num);
		numberBox.textboxKeyTyped(character, num);
		
		if(!searchBox.getText().equalsIgnoreCase(prevTxt))
		{
			searchPage = 0;
			searchResults.clear();
			searchTxt = searchBox.getText().toLowerCase();
			searching = FluidRegistry.getRegisteredFluids().values().iterator();
		}
    }
}
