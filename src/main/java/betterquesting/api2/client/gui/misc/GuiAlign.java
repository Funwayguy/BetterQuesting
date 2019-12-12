package betterquesting.api2.client.gui.misc;

import betterquesting.abs.misc.GuiAnchor;

/**
 * Provides pre-made anchor points for GUIs with functions to quickly create new ones
 */
public class GuiAlign
{
	public static final GuiAnchor FULL_BOX = new GuiAnchor(0F, 0F, 1F, 1F);
	
	public static final GuiAnchor TOP_LEFT = new GuiAnchor(0F, 0F, 0F, 0F);
	public static final GuiAnchor TOP_CENTER = new GuiAnchor(0.5F, 0F, 0.5F, 0F);
	public static final GuiAnchor TOP_RIGHT = new GuiAnchor(1F, 0F, 1F, 0F);
	public static final GuiAnchor TOP_EDGE = new GuiAnchor(0F, 0F, 1F, 0F);
	
	public static final GuiAnchor MID_LEFT = new GuiAnchor(0F, 0.5F, 0F, 0.5F);
	public static final GuiAnchor MID_CENTER = new GuiAnchor(0.5F, 0.5F, 0.5F, 0.5F);
	public static final GuiAnchor MID_RIGHT = new GuiAnchor(1F, 0.5F, 1F, 0.5F);
	
	public static final GuiAnchor BOTTOM_LEFT = new GuiAnchor(0F, 1F, 0F, 1F);
	public static final GuiAnchor BOTTOM_CENTER = new GuiAnchor(0.5F, 1F, 0.5F, 1F);
	public static final GuiAnchor BOTTOM_RIGHT = new GuiAnchor(1F, 1F, 1F, 1F);
	public static final GuiAnchor BOTTOM_EDGE = new GuiAnchor(0F, 1F, 1F, 1F);
	
	public static final GuiAnchor HALF_LEFT = new GuiAnchor(0F, 0F, 0.5F, 1F);
	public static final GuiAnchor HALF_RIGHT = new GuiAnchor(0.5F, 0F, 1F, 1F);
	public static final GuiAnchor HALF_TOP = new GuiAnchor(0F, 0F, 1F, 0.5F);
	public static final GuiAnchor HALF_BOTTOM = new GuiAnchor(0F, 0.5F, 1F, 1F);
	
	public static final GuiAnchor LEFT_EDGE = new GuiAnchor(0F, 0F, 0F, 1F);
	public static final GuiAnchor RIGHT_EDGE = new GuiAnchor(1F, 0F, 1F, 1F);
	
	/**
	 * Takes two readable Vector4f points and merges them in a single Vector4f anchor region
	 */
	public static GuiAnchor quickAnchor(GuiAnchor v1, GuiAnchor v2)
	{
		float x1 = Math.min(v1.getX(), v2.getX());
		float y1 = Math.min(v1.getY(), v2.getY());
		float x2 = Math.max(v1.getZ(), v2.getZ());
		float y2 = Math.max(v1.getW(), v2.getW());
		
		return new GuiAnchor(x1, y1, x2, y2);
	}
}
