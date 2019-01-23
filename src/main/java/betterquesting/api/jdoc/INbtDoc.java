package betterquesting.api.jdoc;

/** Used to self document the NBT editors with localised tooltips and variable naming */
public interface INbtDoc
{
	String getUnlocalisedTitle();
	
	String getUnlocalisedName(String key);
	String getUnlocalisedDesc(String key);
	
	INbtDoc getParent();
	INbtDoc getChild(String key);
}
