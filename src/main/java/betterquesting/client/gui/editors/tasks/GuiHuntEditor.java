package betterquesting.client.gui.editors.tasks;

import java.awt.Color;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.monster.EntityZombie;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.MathHelper;
import org.lwjgl.opengl.GL11;
import betterquesting.client.gui.GuiQuesting;
import betterquesting.client.gui.editors.json.GuiJsonEntitySelection;
import betterquesting.client.gui.editors.json.GuiJsonObject;
import betterquesting.client.gui.misc.GuiButtonQuesting;
import betterquesting.client.gui.misc.GuiNumberField;
import betterquesting.utils.JsonHelper;
import betterquesting.utils.NBTConverter;
import betterquesting.utils.RenderUtils;
import com.google.gson.JsonObject;

public class GuiHuntEditor extends GuiQuesting
{
	GuiNumberField numField;
	int amount = 1;
	String idName = "Zombie";
	JsonObject data;
	JsonObject lastEdit = null;
	Entity entity;
	
	public GuiHuntEditor(GuiScreen parent, JsonObject data)
	{
		super(parent, "Edit Task Hunt");
		this.data = data;
		idName = JsonHelper.GetString(data, "target", "Zombie");
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public void initGui()
	{
		super.initGui();
		
		if(lastEdit != null)
		{
			idName = JsonHelper.GetString(lastEdit, "id", "Zombie");
			
			entity = EntityList.createEntityByName(idName, mc.theWorld);
			
			if(entity == null)
			{
				entity = new EntityZombie(mc.theWorld);
				idName = "Zombie";
			}
			
			lastEdit = null;
			data.addProperty("target", idName);
		}
		
		numField = new GuiNumberField(mc.fontRenderer, guiLeft + sizeX/2 + 1, guiTop + sizeY/2 + 1, 98, 18);
		numField.setText("" + JsonHelper.GetNumber(data, "required", 1).intValue());
		this.buttonList.add(new GuiButtonQuesting(buttonList.size(), guiLeft + sizeX/2 - 100, guiTop + sizeY/2 + 20, 200, 20, "Select Mob"));
		this.buttonList.add(new GuiButtonQuesting(buttonList.size(), guiLeft + sizeX/2 - 100, guiTop + sizeY/2 + 40, 200, 20, "Advanced"));
	}
	
	@Override
	public void drawScreen(int mx, int my, float partialTick)
	{
		super.drawScreen(mx, my, partialTick);
		
		if(entity == null)
		{
			entity = EntityList.createEntityByName(idName, mc.theWorld);
			
			if(entity == null)
			{
				entity = new EntityZombie(mc.theWorld);
				idName = "Zombie";
			}
		}
		
		GL11.glPushMatrix();
		
		GL11.glColor4f(1F, 1F, 1F, 1F);
		
		float angle = ((float)Minecraft.getSystemTime()%30000F)/30000F * 360F;
		float scale = 64F;
		
		if(entity.height * scale > (sizeY/2 - 52))
		{
			scale = (sizeY/2 - 52)/entity.height;
		}
		
		if(entity.width * scale > sizeX)
		{
			scale = sizeX/entity.width;
		}
		
		try
		{
			RenderUtils.RenderEntity(guiLeft + sizeX/2, guiTop + sizeY/4 + MathHelper.ceiling_float_int(entity.height/2F*scale) + 16, (int)scale, angle, 0F, entity);
		} catch(Exception e)
		{
		}
		
		GL11.glPopMatrix();
		
		String txt = "Amount: ";
		mc.fontRenderer.drawString("Amount:", guiLeft + sizeX/2 - mc.fontRenderer.getStringWidth(txt), guiTop + sizeY/2 + 6, Color.BLACK.getRGB());
		numField.drawTextBox();
	}
	
	@Override
	public void actionPerformed(GuiButton button)
	{
		super.actionPerformed(button);
		
		if(button.id == 1)
		{
			if(entity != null)
			{
				NBTTagCompound eTags = new NBTTagCompound();
				entity.writeToNBTOptional(eTags);
				lastEdit = NBTConverter.NBTtoJSON_Compound(eTags, new JsonObject());
				mc.displayGuiScreen(new GuiJsonEntitySelection(this, lastEdit));
			}
		} else if(button.id == 2)
		{
			mc.displayGuiScreen(new GuiJsonObject(this, data));
		}
	}
	
    /**
     * Called when the mouse is clicked.
     */
	@Override
    protected void mouseClicked(int mx, int my, int click)
    {
		super.mouseClicked(mx, my, click);
		
		numField.mouseClicked(mx, my, click);
    }

    /**
     * Fired when a key is typed. This is the equivalent of KeyListener.keyTyped(KeyEvent e).
     */
	@Override
    protected void keyTyped(char character, int keyCode)
    {
        super.keyTyped(character, keyCode);
        
        numField.textboxKeyTyped(character, keyCode);
		data.addProperty("required", numField.getNumber().intValue());
    }
	
	/*  ........
	 *  {render}
	 *  { mob  }
	 *  { here }
	 *  ........
	 * 
	 * Amount:
	 * [number_field]
	 * [Done][Advanced]
	 */
}
