package betterquesting.api.enums;

/**
 * List of commonly used actions in packet handling
 */
public enum EnumPacketAction
{
	// === GENERAL ===
	SYNC,	// Sync configuration and progress
	EDIT,	// Edit configuration
	ADD,	// Add new entry
	REMOVE,	// Remove existing entry
	SET,	// Set state (reward/task completion)
	
	// === PARTY ===
	INVITE,	// Invite to party
	KICK,	// Kick from party
	JOIN;	// Join party
}
