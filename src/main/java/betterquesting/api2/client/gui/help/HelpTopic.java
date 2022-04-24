package betterquesting.api2.client.gui.help;

import betterquesting.api2.utils.QuestTranslation;

import javax.annotation.Nonnull;

public class HelpTopic {
    private final String title;
    private final String description;

    public HelpTopic(@Nonnull String title, @Nonnull String description) {
        this.title = title;
        this.description = description;
    }

    public String getTitle() {
        return QuestTranslation.translate(title);
    }

    public String getDescription() {
        return QuestTranslation.translate(description);
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof HelpTopic)) return false;

        HelpTopic ht = (HelpTopic) o;

        return ht.title.equals(this.title) && ht.description.equals(this.description);
    }
}
