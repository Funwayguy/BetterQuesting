package betterquesting.client.gui2.editors.designer;

import betterquesting.api.client.toolbox.IToolboxTool;
import betterquesting.api.utils.RenderUtils;
import betterquesting.api2.client.gui.controls.IValueIO;
import betterquesting.api2.client.gui.misc.IGuiRect;
import betterquesting.api2.client.gui.panels.IGuiPanel;
import betterquesting.client.gui2.CanvasQuestLine;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.math.MathHelper;

import java.util.List;

// Kinda just a poxy panel where tools can be hotswapped out
public class PanelToolController implements IGuiPanel
{
    private final CanvasQuestLine questLine;
    private final IGuiRect transform;
    private boolean enabled = true;
    
    private final IValueIO<Float> scDriverX;
    private final IValueIO<Float> scDriverY;
    
    private IToolboxTool activeTool;
    
    public PanelToolController(IGuiRect rect, CanvasQuestLine questLine)
    {
        this.transform = rect;
        this.questLine = questLine;
        
        scDriverX = new IValueIO<Float>()
        {
            private float value = 0F;
            
            @Override
            public Float readValue()
            {
                return value;
            }
    
            @Override
            public void writeValue(Float value)
            {
                if(activeTool != null && !activeTool.clampScrolling())
                {
                    this.value = value;
                } else
                {
                    this.value = MathHelper.clamp(value, 0F, 1F);
                }
            }
        };
        
        scDriverY = new IValueIO<Float>()
        {
            private float value = 0F;
            
            @Override
            public Float readValue()
            {
                return value;
            }
    
            @Override
            public void writeValue(Float value)
            {
                if(activeTool != null && !activeTool.clampScrolling())
                {
                    this.value = value;
                } else
                {
                    this.value = MathHelper.clamp(value, 0F, 1F);
                }
            }
        };
    }
    
    public void setActiveTool(IToolboxTool tool)
    {
        if(this.activeTool != null) activeTool.disableTool();
        if(tool == null) return;
        
        activeTool = tool;
        tool.initTool(questLine);
    }
    
    public IValueIO<Float> getScrollX()
    {
        return this.scDriverX;
    }
    
    public IValueIO<Float> getScrollY()
    {
        return this.scDriverY;
    }
    
    @Override
    public IGuiRect getTransform()
    {
        return transform;
    }
    
    @Override
    public void initPanel()
    {
    }
    
    @Override
    public void setEnabled(boolean state)
    {
        this.enabled = state;
    }
    
    @Override
    public boolean isEnabled()
    {
        return enabled;
    }
    
    @Override
    public void drawPanel(int mx, int my, float partialTick)
    {
        if(!enabled) return;
        
        if(activeTool != null)
        {
            float zs = questLine.getZoom();
            int lsx = questLine.getScrollX();
            int lsy = questLine.getScrollY();
            int tx = getTransform().getX();
            int ty = getTransform().getY();
            int smx = (int)((mx - tx) / zs) + lsx;
            int smy = (int)((my - ty) / zs) + lsy;
    
            GlStateManager.pushMatrix();
            RenderUtils.startScissor(transform);
            
            GlStateManager.translate(tx - lsx * zs, ty - lsy * zs, 0F);
		    GlStateManager.scale(zs, zs, zs);
      
		    // Pretending we're on the scrolling canvas (when we're really not) so as not to influence it by hotswapping panels
            activeTool.drawCanvas(smx, smy, partialTick);
            
            RenderUtils.endScissor();
            GlStateManager.popMatrix();
            
            activeTool.drawOverlay(mx, my, partialTick);
        }
    }
    
    @Override
    public boolean onMouseClick(int mx, int my, int button)
    {
        if(activeTool != null) return activeTool.onMouseClick(mx, my, button);
        return false;
    }
    
    @Override
    public boolean onMouseRelease(int mx, int my, int button)
    {
        if(activeTool != null) return activeTool.onMouseRelease(mx, my, button);
        return false;
    }
    
    @Override
    public boolean onMouseScroll(int mx, int my, int scroll)
    {
        if(activeTool != null) return activeTool.onMouseScroll(mx, my, scroll);
        return false;
    }
    
    @Override
    public boolean onKeyTyped(char c, int keycode)
    {
        if(activeTool != null) return activeTool.onKeyPressed(c, keycode);
        return false;
    }
    
    @Override
    public List<String> getTooltip(int mx, int my)
    {
        if(activeTool != null) return activeTool.getTooltip(mx, my);
        return null;
    }
}
