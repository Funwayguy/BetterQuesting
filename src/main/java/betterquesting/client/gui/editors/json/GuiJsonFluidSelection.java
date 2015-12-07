package betterquesting.client.gui.editors.json;

import java.util.ArrayList;
import java.util.Iterator;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.StatCollector;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidContainerRegistry;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import org.apache.logging.log4j.Level;
import org.lwjgl.opengl.GL11;
import betterquesting.client.gui.GuiQuesting;
import betterquesting.client.gui.misc.GuiButtonQuesting;
import betterquesting.client.themes.ThemeRegistry;
import betterquesting.core.BetterQuesting;
import betterquesting.utils.NBTConverter;
import betterquesting.utils.RenderUtils;
import com.google.gson.JsonObject;
import com.mojang.realmsclient.gui.ChatFormatting;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class GuiJsonFluidSelection extends GuiQuesting
{
	FluidStack stackSelect;
	JsonObject json;
	GuiTextField searchBox;
	ArrayList<FluidStack> searchResults = new ArrayList<FluidStack>();
	int searchPage = 0;
	int rows = 1;
	int columns = 1;
	
	public GuiJsonFluidSelection(GuiScreen parent, JsonObject json)
	{
		super(parent, "betterquesting.title.select_fluid");
		this.json = json;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public void initGui()
	{
		super.initGui();
		this.searchBox = new GuiTextField(this.fontRendererObj, -999, -999, 128, 16);
		this.searchBox.setMaxStringLength(Integer.MAX_VALUE);

		Iterator<Fluid> iterator = FluidRegistry.getRegisteredFluids().values().iterator();
		
		while(iterator.hasNext())
		{
			Fluid fluid = iterator.next();
			
			if(fluid != null)
			{
				searchResults.add(new FluidStack(fluid, 1000));
			}
		}
		
		if(json != null)
		{
			stackSelect = FluidStack.loadFluidStackFromNBT(NBTConverter.JSONtoNBT_Object(json, new NBTTagCompound()));
		} else if(stackSelect != null)
		{
			json.entrySet().clear();
			NBTConverter.NBTtoJSON_Compound(stackSelect.writeToNBT(new NBTTagCompound()), json);
		}
		
		if(stackSelect == null)
		{
			BetterQuesting.logger.log(Level.ERROR, "The JSON editor was unable to parse fluid NBTs! Reverting to water...");
			stackSelect = new FluidStack(FluidRegistry.WATER, 1000);
		}
		
		NBTConverter.NBTtoJSON_Compound(stackSelect.writeToNBT(new NBTTagCompound()), json);
		
		GuiButtonQuesting leftBtn = new GuiButtonQuesting(1, this.guiLeft + this.sizeX/2, this.guiTop + this.sizeY - 48, 20, 20, "<");
		this.buttonList.add(leftBtn);
		GuiButtonQuesting rightBtn = new GuiButtonQuesting(2, this.guiLeft + this.sizeX - 36, this.guiTop + this.sizeY - 48, 20, 20, ">");
		this.buttonList.add(rightBtn);
	}
	
	@Override
	public void drawScreen(int mx, int my, float partialTick)
	{
		super.drawScreen(mx, my, partialTick);
		
		FluidStack ttStack = null;
		
		GL11.glColor4f(1f, 1f, 1f, 1f);

		this.mc.renderEngine.bindTexture(ThemeRegistry.curTheme().guiTexture());
		this.drawTexturedModalRect(this.guiLeft + 16 , this.guiTop + 48, 0, 48, 18, 18);
		
		this.fontRendererObj.drawString(I18n.format("betterquesting.gui.selection"), this.guiLeft + 16, this.guiTop + 32, ThemeRegistry.curTheme().textColor().getRGB(), false);
		
		if(this.stackSelect != null)
		{
			mc.renderEngine.bindTexture(TextureMap.locationBlocksTexture);
			GL11.glColor4f(1F, 1F, 1F, 1F);
			RenderUtils.itemRender.renderIcon(this.guiLeft + 17, this.guiTop + 49, stackSelect.getFluid().getIcon(), 16, 16);
			
			this.fontRendererObj.drawString(this.stackSelect.getLocalizedName() + " x " + this.stackSelect.amount + "mB", this.guiLeft + 36, this.guiTop + 52, ThemeRegistry.curTheme().textColor().getRGB(), false);
			
			if(this.isWithin(mx, my, 16, 48, 16, 16))
			{
				ttStack = this.stackSelect;
			}
		}
		
		this.fontRendererObj.drawString(I18n.format("container.inventory"), this.guiLeft + 16, this.guiTop + 80, ThemeRegistry.curTheme().textColor().getRGB(), false);
		
		GL11.glColor4f(1F, 1F, 1F, 1F);
		
		if(this.mc.thePlayer != null)
		{
			ItemStack[] invoStacks = this.mc.thePlayer.inventory.mainInventory;
			
			for(int i = 0; i < invoStacks.length; i++)
			{
				int x = i%9 * 18;
				int y = (i - i%9)/9 * 18;
				
				this.mc.renderEngine.bindTexture(ThemeRegistry.curTheme().guiTexture());
				GL11.glDisable(GL11.GL_DEPTH_TEST);
				this.drawTexturedModalRect(this.guiLeft + 16 + x , this.guiTop + 96 + y, 0, 48, 18, 18);
				GL11.glEnable(GL11.GL_DEPTH_TEST);
				
				ItemStack stack = invoStacks[i];
				
				if(stack != null)
				{
					RenderUtils.RenderItemStack(this.mc, stack, this.guiLeft + 17 + x, this.guiTop + 97 + y, "" + (stack.stackSize > 1? stack.stackSize : ""));
					
					if(this.isWithin(mx, my, 17 + x, 97 + y, 16, 16) && FluidContainerRegistry.isFilledContainer(stack))
					{
						ttStack = FluidContainerRegistry.getFluidForFilledItem(stack);
					}
				}
			}
		}

		this.fontRendererObj.drawString(I18n.format("betterquesting.gui.search"), this.guiLeft + this.sizeX/2, this.guiTop + 32, ThemeRegistry.curTheme().textColor().getRGB(), false);
		this.searchBox.xPosition = this.guiLeft + this.width/2 + this.fontRendererObj.getStringWidth("Search: ");
		this.searchBox.yPosition = this.guiTop + 28;
		this.searchBox.drawTextBox();
		
		GL11.glColor4f(1f, 1f, 1f, 1f);
		
		int x = 0;
		int y = 0;
		columns = (sizeX/2 - 18)/18;
		rows = (this.sizeY - (48 + 48))/18;
		
		for(int i = (columns * rows * searchPage); i < searchResults.size(); i++)
		{
			int n = i - (columns * rows * searchPage);
			x = n%columns * 18;
			y = (n - n%columns)/columns * 18;
			
			if(y > this.sizeY - (48 + 48 + 18))
			{
				break;
			}
			
			this.mc.renderEngine.bindTexture(ThemeRegistry.curTheme().guiTexture());
			
			FluidStack resultStack = searchResults.get(i);
			
			if(resultStack != null)
			{
				GL11.glDisable(GL11.GL_DEPTH_TEST);
				this.drawTexturedModalRect(this.guiLeft + this.sizeX/2 + x , this.guiTop + 48 + y, 0, 48, 18, 18);
				GL11.glEnable(GL11.GL_DEPTH_TEST);
				
				mc.renderEngine.bindTexture(TextureMap.locationBlocksTexture);
				GL11.glColor4f(1F, 1F, 1F, 1F);
				RenderUtils.itemRender.renderIcon(this.guiLeft + this.sizeX/2 + 1 + x, this.guiTop + 49 + y, resultStack.getFluid().getIcon(), 16, 16);
				
				if(this.isWithin(mx, my, this.sizeX/2 + x + 1, 49 + y, 16, 16))
				{
					ttStack = resultStack;
				}
			}
		}
		
		if(ttStack != null)
		{
			ArrayList<String> tTip = new ArrayList<String>();
			tTip.add(ttStack.getLocalizedName());
			tTip.add(ChatFormatting.GRAY + "" + ttStack.amount + " mB");
			this.DrawTooltip(tTip, mx, my);
		}
	}
	
	@Override
	public void actionPerformed(GuiButton button)
	{
		super.actionPerformed(button);
		
		if(button.id == 1 && searchPage > 0)
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
		
		if(this.mc.thePlayer != null && this.isWithin(mx, my, 16, 96, 18 * 9, 18 * this.mc.thePlayer.inventory.mainInventory.length/9))
		{
			int sx = (mx - (this.guiLeft + 16))/18;
			int sy = (my - (this.guiTop + 96))/18;
			int index = sx + (sy * 9);
			
			if(index >= 0 && index < this.mc.thePlayer.inventory.mainInventory.length)
			{
				ItemStack invoStack = this.mc.thePlayer.inventory.mainInventory[index];
				
				if(invoStack != null && FluidContainerRegistry.isFilledContainer(invoStack))
				{
					this.stackSelect = FluidContainerRegistry.getFluidForFilledItem(invoStack).copy();
					this.json.entrySet().clear();
					this.json = NBTConverter.NBTtoJSON_Compound(this.stackSelect.writeToNBT(new NBTTagCompound()), this.json);
				}
			}
		} else if(this.isWithin(mx, my, this.sizeX/2, 48, columns * 18, rows * 18))
		{

			int sx = (mx - (this.guiLeft + this.sizeX/2))/18;
			int sy = (my - (this.guiTop + 48))/18;
			int index = sx + (sy * columns) + (searchPage * columns * rows);
			
			if(index >= 0 && index < this.searchResults.size())
			{
				FluidStack searchFluid = this.searchResults.get(index);
				
				if(searchFluid != null)
				{
					this.stackSelect = searchFluid.copy();
					this.json.entrySet().clear();
					this.json = NBTConverter.NBTtoJSON_Compound(this.stackSelect.writeToNBT(new NBTTagCompound()), this.json);
				}
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
		
		if(!searchBox.getText().equalsIgnoreCase(prevTxt))
		{
			searchPage = 0;
			searchResults.clear();
			String searchTxt = searchBox.getText().toLowerCase();
			
			Iterator<Fluid> iterator = FluidRegistry.getRegisteredFluids().values().iterator();
			
			while(iterator.hasNext())
			{
				Fluid baseFluid = iterator.next();
				
				if(baseFluid == null)
				{
					continue;
				}
				
				if(baseFluid.getUnlocalizedName().toLowerCase().contains(searchTxt) || StatCollector.translateToLocal(baseFluid.getUnlocalizedName()).toLowerCase().contains(searchTxt) || FluidRegistry.getDefaultFluidName(baseFluid).toLowerCase().contains(searchTxt))
				{
					searchResults.add(new FluidStack(baseFluid, 1000));
				}
			}
		}
    }
}
