package betterquesting.api.api;

@Deprecated /** Just use the post init method. This doesn't do anything special anymore*/
public interface IQuestExpansion
{
	/**
	 * Called after BetterQuesting's APIs have been initialized
	 */
	void loadExpansion();
}
