package betterquesting.client.gui;

import java.awt.image.BufferedImage;
import java.io.File;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.texture.TextureUtil;
import org.lwjgl.opengl.GL11;
import org.monte.media.AudioFormatKeys;
import org.monte.media.Buffer;
import org.monte.media.FormatKeys;
import org.monte.media.FormatKeys.MediaType;
import org.monte.media.VideoFormatKeys;
import org.monte.media.avi.AVIReader;
import org.monte.media.math.Rational;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * An experimental GUI for AVI cut scenes. Audio is a bit choppy but otherwise works fairly well despite limited codec support
 */
@SideOnly(Side.CLIENT)
public class GuiCutscene extends GuiScreen
{
	File file;
	AVIReader avi;
	BufferedImage frame;
	Buffer aBuff;
	Clip aClip;
	AudioFormat aForm;
	int vTrack = 0;
	int aTrack = 0;
	
	public GuiCutscene(File file)
	{
		this.file = file;
	}
	
	@Override
	public void initGui()
	{
		super.initGui();
		startTime = -1;
		try
		{
			avi = new AVIReader(file);
			
			int t = 0;
			boolean vid = false;
			boolean aud = false;
			
		    while (t < avi.getTrackCount() && (!vid || !aud))
		    {
		    	MediaType media = avi.getFormat(t).get(VideoFormatKeys.MediaTypeKey);
		    	
		    	if(!vid && media == FormatKeys.MediaType.VIDEO)
		    	{
		    		vid = true;
		    		vTrack = t;
		    	}
		    	
		    	if(!aud && media == FormatKeys.MediaType.AUDIO)
		    	{
		    		aud = true;
		    		aTrack = t;
		    	}
		    	
	    		t++;
		    }
		} catch(Exception e)
		{
			e.printStackTrace();
			this.mc.displayGuiScreen(null);
		}
	}
	
	int glID = -1;
	
	long startTime = -1;
	
	@Override
	public void drawScreen(int mx, int my, float partialTick)
	{
		if(startTime < 0)
		{
			startTime = Minecraft.getSystemTime();
		}
		
		if(avi != null)
		{
			GL11.glPushMatrix();
			
			try
			{
				Rational curTime = new Rational(Minecraft.getSystemTime() - startTime, 1000L);
				
				if(curTime.compareTo(avi.getDuration()) >= 0)
				{
					if(aClip != null)
					{
						aClip.close();
					}
					avi.close();
					this.mc.displayGuiScreen(null);
					return;
				}
				
				avi.setMovieReadTime(curTime);
					
				if(aBuff == null)
				{
					aBuff = new Buffer();
				}
				
				if(aClip == null)
				{
					aClip = AudioSystem.getClip();
				}
				
				avi.read(aTrack, aBuff);
				
				if(aForm == null)
				{
					if(aBuff.format.get(AudioFormatKeys.SampleSizeInBitsKey, 16) <= 0)
					{
						aBuff.format.properties.put(AudioFormatKeys.SampleSizeInBitsKey, 16); // Force non 0 bitrate
					}
					aForm = AudioFormatKeys.toAudioFormat(aBuff.format);
				}
				
				if(!aClip.isActive())
				{
					aClip.flush();
					aClip.close();
					aClip.open(aForm, (byte[])aBuff.data, 0, aBuff.length);
					aClip.start();
				}
				
				frame = avi.read(vTrack, frame);
				
				if(frame != null)
				{
					if(glID < 0)
					{
						glID = TextureUtil.glGenTextures();
					}
					TextureUtil.uploadTextureImage(glID, frame);
					GL11.glBindTexture(GL11.GL_TEXTURE_2D, glID);
					double scaleX = this.width/Math.min(256D, (double)frame.getWidth());
					double scaleY = this.height/Math.min(256D, (double)frame.getHeight()); 
					GL11.glScaled(scaleX, scaleY, 1D);
					this.drawTexturedModalRect(0, 0, 0, 0, frame.getWidth(), frame.getHeight());
				}
			} catch(Exception e)
			{
				e.printStackTrace();
				this.mc.displayGuiScreen(null);
			}
			
			GL11.glPopMatrix();
		}
	}
}
