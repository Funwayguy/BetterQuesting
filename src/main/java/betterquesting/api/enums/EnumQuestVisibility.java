package betterquesting.api.enums;

public enum EnumQuestVisibility {
    HIDDEN, // Never shown, and hidden from view mode.
    SECRET, // Like UNLOCKED, except also hidden from view mode.
    UNLOCKED, // Must be unlocked to be shown (all prerequisites must be completed).
    NORMAL, // Will be shown if all prerequisites are unlocked.
    COMPLETED, // Must be completed to be shown.
    CHAIN, // Will be shown if all prerequisites are shown.
    ALWAYS; // Always shown.
}
