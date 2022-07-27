package betterquesting.api.nbt_doc;

import betterquesting.api.events.NbtDocEvent;
import net.minecraftforge.common.MinecraftForge;

public class NbtDocBasic implements INbtDoc {
    private final INbtDoc parent;
    private final String prefix;

    public NbtDocBasic(INbtDoc parent, String prefix) {
        this.parent = parent;
        this.prefix = prefix;
    }

    @Override
    public String getUnlocalisedTitle() {
        return prefix + ".name";
    }

    @Override
    public String getUnlocalisedName(String key) {
        return prefix + "." + key + ".name";
    }

    @Override
    public String getUnlocalisedDesc(String key) {
        return prefix + "." + key + ".desc";
    }

    @Override
    public INbtDoc getParent() {
        return parent;
    }

    @Override
    public INbtDoc getChild(String child) {
        NbtDocEvent event = new NbtDocEvent(new NbtDocBasic(this, prefix + "." + child));
        MinecraftForge.EVENT_BUS.post(event);
        return event.getNbtDocResult();
    }
}
