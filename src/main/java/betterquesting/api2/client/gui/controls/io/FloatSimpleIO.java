package betterquesting.api2.client.gui.controls.io;

import betterquesting.api.utils.RenderUtils;
import betterquesting.api2.client.gui.controls.IValueIO;
import net.minecraft.util.math.MathHelper;

public class FloatSimpleIO implements IValueIO<Float>
{
    private boolean lerp = false;
    private float lerpSpd = 0.02F;
    
    private boolean clamp = true;
    private final float min;
    private final float max;
    
    protected float v; // Target value
    protected float s; // Start value
    private long t; // Start time
    
    public FloatSimpleIO()
    {
        this(0F, 0F, 1F);
    }
    
    public FloatSimpleIO(float startVal, float min, float max)
    {
        this.v = startVal;
        this.s = startVal;
        this.min = min;
        this.max = max;
        this.t = System.currentTimeMillis(); // Added precaution for lerp math
    }
    
    public FloatSimpleIO setClamp(boolean enable)
    {
        this.clamp = enable;
        return this;
    }
    
    public FloatSimpleIO setLerp(boolean enable, float speed)
    {
        this.lerp = enable;
        this.lerpSpd = speed;
        return this;
    }
    
    @Override
    public Float readValue()
    {
        if(lerp && s != v)
        {
            if(Math.abs(s - v) < 0.001F)
            {
                s = v;
                return v;
            }

            long tmpMillis = System.currentTimeMillis();
            long d = tmpMillis - t;
            s = RenderUtils.lerpFloat(s, v, (float)MathHelper.clamp(d * (double)lerpSpd, 0D, 1D));
            if(d > 0) t = tmpMillis; // Required if read out more than once in 1ms
            return s;
        }
        
        return v;
    }
    
    @Override
    public void writeValue(Float value)
    {
        if(s == v) t = System.currentTimeMillis();
        this.v = clamp ? MathHelper.clamp(value, min, max) : value;
    }
    
    @Override
    public Float readValueRaw()
    {
        return this.v;
    }
    
    @Override
    public void writeValueRaw(Float value)
    {
        this.v = clamp ? MathHelper.clamp(value, min, max) : value;
        this.s = v;
    }
}
