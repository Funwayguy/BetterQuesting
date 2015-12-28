package betterquesting.quests.designers;

import java.util.ArrayList;

public class DesignRegistry
{
	static ArrayList<QDesign> designs = new ArrayList<QDesign>();
	
	public void RegisterDesign(QDesign des)
	{
		if(des == null)
		{
			throw new NullPointerException("Tried to register NULL quest line designer");
		} else if(designs.contains(des))
		{
			throw new IllegalArgumentException("Tried to register duplicate quest line design");
		}
		
		designs.add(des);
	}
	
	public ArrayList<QDesign> getDesigns()
	{
		return new ArrayList<QDesign>(designs);
	}
}
