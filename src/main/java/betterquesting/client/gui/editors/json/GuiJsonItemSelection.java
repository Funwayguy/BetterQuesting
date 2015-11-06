package betterquesting.client.gui.editors.json;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.StatCollector;
import org.apache.logging.log4j.Level;
import org.lwjgl.opengl.GL11;
import betterquesting.client.gui.GuiQuesting;
import betterquesting.client.gui.misc.GuiButtonQuesting;
import betterquesting.core.BetterQuesting;
import betterquesting.utils.BigItemStack;
import betterquesting.utils.JsonHelper;
import betterquesting.utils.NBTConverter;
import betterquesting.utils.RenderUtils;
import com.google.gson.JsonObject;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class GuiJsonItemSelection extends GuiQuesting
{
	BigItemStack stackSelect;
	JsonObject json;
	GuiTextField searchBox;
	ArrayList<ItemStack> searchResults = new ArrayList<ItemStack>();
	int searchPage = 0;
	int rows = 1;
	int columns = 1;
	
	public GuiJsonItemSelection(GuiScreen parent, JsonObject json)
	{
		super(parent, "Select Item");
		this.json = json;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public void initGui()
	{
		super.initGui();
		this.searchBox = new GuiTextField(this.fontRendererObj, -999, -999, 128, 16);
		this.searchBox.setMaxStringLength(Integer.MAX_VALUE);

		Iterator<Item> iterator = Item.itemRegistry.iterator();
		
		while(iterator.hasNext())
		{
			Item item = iterator.next();
			
			if(item != null)
			{
				item.getSubItems(item, CreativeTabs.tabAllSearch, searchResults);
			}
		}
		
		if(json != null)
		{
			stackSelect = JsonHelper.JsonToItemStack(json);
		} else if(stackSelect != null)
		{
			json.entrySet().clear();
			JsonHelper.ItemStackToJson(stackSelect, json);
		}
		
		if(stackSelect == null)
		{
			BetterQuesting.logger.log(Level.ERROR, "The JSON editor was unable to parse item NBTs! Reverting to stone...");
			stackSelect = new BigItemStack(Blocks.stone);
			json.entrySet().clear();
			JsonHelper.ItemStackToJson(stackSelect, json);
		} else // Ensure all necessary NBTs are present
		{
			json.entrySet().clear();
			JsonHelper.ItemStackToJson(stackSelect, json);
		}
		
		GuiButtonQuesting leftBtn = new GuiButtonQuesting(1, this.guiLeft + this.sizeX/2, this.guiTop + this.sizeY - 48, 20, 20, "<");
		this.buttonList.add(leftBtn);
		GuiButtonQuesting rightBtn = new GuiButtonQuesting(2, this.guiLeft + this.sizeX - 36, this.guiTop + this.sizeY - 48, 20, 20, ">");
		this.buttonList.add(rightBtn);
	}
	
	@Override
	public void drawScreen(int mx, int my, float partialTick)
	{
		super.drawScreen(mx, my, partialTick);
		
		BigItemStack ttStack = null;
		
		GL11.glColor4f(1f, 1f, 1f, 1f);

		this.mc.renderEngine.bindTexture(guiTexture);
		this.drawTexturedModalRect(this.guiLeft + 16 , this.guiTop + 48, 0, 48, 18, 18);
		RenderUtils.RenderItemStack(this.mc, this.stackSelect.getBaseStack(), this.guiLeft + 17, this.guiTop + 49, "");
		
		this.fontRendererObj.drawString("Selection", this.guiLeft + 16, this.guiTop + 32, Color.BLACK.getRGB(), false);
		
		if(this.stackSelect != null)
		{
			this.fontRendererObj.drawString(this.stackSelect.getBaseStack().getDisplayName() + " x " + this.stackSelect.stackSize, this.guiLeft + 36, this.guiTop + 52, Color.BLACK.getRGB(), false);
			
			if(this.isWithin(mx, my, 16, 48, 16, 16))
			{
				ttStack = this.stackSelect;
			}
		}
		
		this.fontRendererObj.drawString("Inventory", this.guiLeft + 16, this.guiTop + 80, Color.BLACK.getRGB(), false);
		
		GL11.glColor4f(1F, 1F, 1F, 1F);
		
		if(this.mc.thePlayer != null)
		{
			ItemStack[] invoStacks = this.mc.thePlayer.inventory.mainInventory;
			
			for(int i = 0; i < invoStacks.length; i++)
			{
				int x = i%9 * 18;
				int y = (i - i%9)/9 * 18;
				
				this.mc.renderEngine.bindTexture(guiTexture);
				GL11.glDisable(GL11.GL_DEPTH_TEST);
				this.drawTexturedModalRect(this.guiLeft + 16 + x , this.guiTop + 96 + y, 0, 48, 18, 18);
				GL11.glEnable(GL11.GL_DEPTH_TEST);
				
				ItemStack stack = invoStacks[i];
				
				if(stack != null)
				{
					RenderUtils.RenderItemStack(this.mc, stack, this.guiLeft + 17 + x, this.guiTop + 97 + y, "" + (stack.stackSize > 1? stack.stackSize : ""));
					
					if(this.isWithin(mx, my, 17 + x, 97 + y, 16, 16))
					{
						ttStack = new BigItemStack(stack);
					}
				}
			}
		}

		this.fontRendererObj.drawString("Search: ", this.guiLeft + this.sizeX/2, this.guiTop + 32, Color.BLACK.getRGB(), false);
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
			
			this.mc.renderEngine.bindTexture(guiTexture);
			
			ItemStack resultStack = searchResults.get(i);
			
			if(resultStack != null)
			{
				GL11.glDisable(GL11.GL_DEPTH_TEST);
				this.drawTexturedModalRect(this.guiLeft + this.sizeX/2 + x , this.guiTop + 48 + y, 0, 48, 18, 18);
				GL11.glEnable(GL11.GL_DEPTH_TEST);
				RenderUtils.RenderItemStack(this.mc, resultStack, this.guiLeft + this.sizeX/2 + 1 + x, this.guiTop + 49 + y, "");
				
				if(this.isWithin(mx, my, this.sizeX/2 + x + 1, 49 + y, 16, 16))
				{
					ttStack = new BigItemStack(resultStack);
				}
			}
		}
		
		if(ttStack != null)
		{
			GL11.glPushMatrix();
			this.drawHoveringText(ttStack.getBaseStack().getTooltip(this.mc.thePlayer, this.mc.gameSettings.advancedItemTooltips), mx, my, this.fontRendererObj);
			GL11.glColor4f(1f, 1f, 1f, 1f);
		    GL11.glDisable(GL11.GL_LIGHTING);
			GL11.glPopMatrix();
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
				
				if(invoStack != null)
				{
					this.stackSelect = new BigItemStack(invoStack.copy());
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
				ItemStack searchItem = this.searchResults.get(index);
				
				if(searchItem != null)
				{
					this.stackSelect = new BigItemStack(searchItem.copy());
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
			
			@SuppressWarnings("unchecked")
			Iterator<Item> iterator = Item.itemRegistry.iterator();
			
			while(iterator.hasNext())
			{
				Item baseItem = iterator.next();
				
				if(baseItem == null)
				{
					continue;
				}
				
				ArrayList<ItemStack> subList = new ArrayList<ItemStack>();
				
				if(baseItem == Items.enchanted_book)
				{
					for(Enchantment enchant : Enchantment.enchantmentsList)
					{
						if(enchant != null)
						{
							Items.enchanted_book.func_92113_a(enchant, subList);
						}
					}
				} else
				{
					baseItem.getSubItems(baseItem, CreativeTabs.tabAllSearch, subList);
				}
				
				if(baseItem.getUnlocalizedName().toLowerCase().contains(searchTxt) || StatCollector.translateToLocal(baseItem.getUnlocalizedName()).toLowerCase().contains(searchTxt) || Item.itemRegistry.getNameForObject(baseItem).toLowerCase().contains(searchTxt))
				{
					searchResults.addAll(subList);
				} else
				{
					for(ItemStack subItem : subList)
					{
						if(subItem != null && (subItem.getUnlocalizedName().toLowerCase().contains(searchTxt) || subItem.getDisplayName().toLowerCase().contains(searchTxt)))
						{
							searchResults.add(subItem);
						} else
						{
							@SuppressWarnings("unchecked")
							List<String> toolTips = subItem.getTooltip(this.mc.thePlayer, this.mc.gameSettings.advancedItemTooltips);
							
							for(String line : toolTips)
							{
								if(line.toLowerCase().contains(searchTxt))
								{
									searchResults.add(subItem);
									break;
								}
							}
						}
					}
				}
			}
		}
    }
	
	/*public void RenderItemStack(ItemStack stack, int x, int y, String text, boolean highlight)
	{
		GL11.glPushMatrix();
        
		try
		{
		    GL11.glColor4f(1F, 1F, 1F, 1F);
			RenderHelper.enableGUIStandardItemLighting();
		    GL11.glEnable(GL12.GL_RESCALE_NORMAL);
			
		    GL11.glTranslatef(0.0F, 0.0F, 32.0F);
		    this.zLevel = 200.0F;
		    itemRender.zLevel = 200.0F;
		    FontRenderer font = null;
		    if (stack != null) font = stack.getItem().getFontRenderer(stack);
		    if (font == null) font = fontRendererObj;
		    itemRender.renderItemAndEffectIntoGUI(font, this.mc.getTextureManager(), stack, x, y);
		    itemRender.renderItemOverlayIntoGUI(font, this.mc.getTextureManager(), stack, x, y, text);
		    this.zLevel = 0.0F;
		    itemRender.zLevel = 0.0F;
		    
		    GL11.glDisable(GL11.GL_LIGHTING);
		} catch(Exception e)
		{
		}
		
        GL11.glPopMatrix();
	}*/
}
