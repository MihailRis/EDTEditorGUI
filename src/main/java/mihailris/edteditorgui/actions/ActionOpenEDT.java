package mihailris.edteditorgui.actions;

import mihailris.edteditorgui.AppContext;
import mihailris.edtfile.EDTItem;

public class ActionOpenEDT implements EditorAction {
    private final EDTItem prev;
    private final EDTItem next;

    public ActionOpenEDT(EDTItem prev, EDTItem next) {
        this.prev = prev;
        this.next = next;
    }

    @Override
    public void action(AppContext context) {
        context.setRoot(next);
        Actions.save();
    }

    @Override
    public void revert(AppContext context) {
        context.setRoot(prev);
    }
}
