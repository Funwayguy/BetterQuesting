package betterquesting.api.events;

import betterquesting.api.nbt_doc.INbtDoc;
import net.minecraftforge.fml.common.eventhandler.Event;

/**
 * Can be used to override the JsonDocs in the editors with custom ones.
 */
public class NbtDocEvent extends Event {
    private final INbtDoc inJdoc;
    private INbtDoc outJdoc;

    public NbtDocEvent(INbtDoc jdoc) {
        inJdoc = jdoc;
        outJdoc = jdoc;
    }

    public INbtDoc getNbtDoc() {
        return inJdoc;
    }

    public void setNewDoc(INbtDoc jdoc) {
        this.outJdoc = jdoc;
    }

    public INbtDoc getNbtDocResult() {
        return outJdoc == null ? inJdoc : outJdoc;
    }
}
