package betterquesting.abs.misc;

// Basically just a Tuple of 4 floats
public class GuiAnchor
{
    private final float x, y, z, w;
    
    public GuiAnchor(float x, float y, float z, float w)
    {
        this.x = x;
        this.y = y;
        this.z = z;
        this.w = w;
    }
    
    public GuiAnchor copy()
    {
        return new GuiAnchor(x, y, z, w);
    }
    
    public float getX()
    {
        return this.x;
    }
    
    public float getY()
    {
        return this.y;
    }
    
    public float getZ()
    {
        return this.z;
    }
    
    public float getW()
    {
        return this.w;
    }
}
