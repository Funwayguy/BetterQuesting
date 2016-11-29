package betterquesting.api.jdoc;

public interface IJsonDoc
{
	public String getUnlocalisedTitle();
	
	public String getUnlocalisedName(String key);
	public String getUnlocalisedDesc(String key);
	
	public IJsonDoc getParentDoc();
	public IJsonDoc getChildDoc(String child);
}
