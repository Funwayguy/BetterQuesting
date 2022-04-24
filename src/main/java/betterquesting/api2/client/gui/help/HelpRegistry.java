package betterquesting.api2.client.gui.help;

import java.util.ArrayList;
import java.util.List;

// Could probably be more refined and fleshed out in functionality at a later date but for now this is just to decouple it from the GUIs
public class HelpRegistry {
    public static final HelpRegistry INSTANCE = new HelpRegistry();

    private final List<HelpTopic> topicList = new ArrayList<>();

    public HelpRegistry() {
        registerTopic(new HelpTopic("betterquesting.btn.help1", "betterquesting.help.page1"));
        registerTopic(new HelpTopic("betterquesting.btn.help2", "betterquesting.help.page2"));
        registerTopic(new HelpTopic("betterquesting.btn.help3", "betterquesting.help.page3"));
        registerTopic(new HelpTopic("betterquesting.btn.help4", "betterquesting.help.page4"));
        registerTopic(new HelpTopic("betterquesting.btn.help5", "betterquesting.help.page5"));
        registerTopic(new HelpTopic("betterquesting.btn.help6", "betterquesting.help.page6"));
        registerTopic(new HelpTopic("betterquesting.btn.help7", "betterquesting.help.page7"));
        registerTopic(new HelpTopic("betterquesting.btn.help8", "betterquesting.help.page8"));
    }

    public boolean registerTopic(HelpTopic topic) {
        if (topicList.contains(topic)) return false;
        return topicList.add(topic);
    }

    public HelpTopic[] getTopics() {
        return topicList.toArray(new HelpTopic[0]);
    }
}
