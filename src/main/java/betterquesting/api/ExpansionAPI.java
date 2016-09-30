package betterquesting.api;

/**
 * API reference for BetterQuesting expansions.
 * Will be initialized when BetterQuesting is loaded.<br>
 */
public final class ExpansionAPI
{
	private static IQuestingAPI API;
	
	public static void initAPI(IQuestingAPI questApi)
	{
		if(API != null)
		{
			throw new IllegalStateException("Cannot initialise BetterQuesting API more than once");
		}
		
		API = questApi;
	}
	
	public static boolean isReady()
	{
		return getAPI() != null;
	}
	
	public static IQuestingAPI getAPI()
	{
		return API;
	}
}
