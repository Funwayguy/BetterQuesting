package betterquesting.api2.client.gui.misc;

import betterquesting.api.utils.RenderUtils;
import net.minecraft.util.math.MathHelper;

import javax.annotation.Nonnull;

public class GuiRectLerp implements IGuiRect
{
    private IGuiRect startRect;
    private IGuiRect targetRect;
    private long duration = 200L;
    private long et;
    
    private final ProxyRect pxRect;
    
    public GuiRectLerp(@Nonnull IGuiRect start)
    {
        this.startRect = start;
        this.targetRect = start;
        this.pxRect = new ProxyRect(start);
        this.et = System.currentTimeMillis();
    }
    
    public void lerpTo(@Nonnull IGuiRect target, long time)
    {
        if(time <= 0) return;
        
        this.targetRect = target;
        this.duration = time;
        this.et = System.currentTimeMillis();
        this.pxRect.changeReference(target);
    }
    
    public void snapTo(@Nonnull IGuiRect target)
    {
        this.startRect = target;
        this.targetRect = target;
        this.et = System.currentTimeMillis();
        this.pxRect.changeReference(target);
    }
    
    // Allows other transforms to snap to new destinations even if the targetRect variable changes instance
    public IGuiRect getProxyRect()
    {
        return pxRect;
    }
    
    private boolean updateLerp()
    {
        if(startRect == targetRect) return true;
        if(System.currentTimeMillis() - et >= duration)
        {
            this.startRect = this.targetRect;
            return true;
        }
        return false;
    }
    
    @Override
    public int getX()
    {
        return updateLerp() ? targetRect.getX() : MathHelper.ceil(RenderUtils.lerpDouble((double)startRect.getX(), (double)targetRect.getX(), (System.currentTimeMillis() - et) / (double)duration));
    }
    
    @Override
    public int getY()
    {
        return updateLerp() ? targetRect.getY() : MathHelper.ceil(RenderUtils.lerpDouble((double)startRect.getY(), (double)targetRect.getY(), (System.currentTimeMillis() - et) / (double)duration));
    }
    
    @Override
    public int getWidth()
    {
        return updateLerp() ? targetRect.getWidth() : MathHelper.ceil(RenderUtils.lerpDouble((double)startRect.getWidth(), (double)targetRect.getWidth(), (System.currentTimeMillis() - et) / (double)duration));
    }
    
    @Override
    public int getHeight()
    {
        return updateLerp() ? targetRect.getHeight() : MathHelper.ceil(RenderUtils.lerpDouble((double)startRect.getHeight(), (double)targetRect.getHeight(), (System.currentTimeMillis() - et) / (double)duration));
    }
    
    @Override
    public int getDepth()
    {
        return targetRect.getDepth();
    }
    
    @Override
    public IGuiRect getParent()
    {
        return targetRect.getParent();
    }
    
    @Override
    public void setParent(IGuiRect rect)
    {
        targetRect.setParent(rect);
    }
    
    @Override
    public boolean contains(int x, int y)
    {
        return targetRect.contains(x, y);
    }
    
    @Override
    public int compareTo(@Nonnull IGuiRect o)
    {
        return targetRect.compareTo(o);
    }
}
