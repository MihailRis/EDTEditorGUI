package mihailris.edteditorgui.actions;

import mihailris.edteditorgui.AppContext;
import mihailris.edtfile.EDTItem;

public class ActionChangeRoot implements EditorAction {
    private final EDTItem prev;
    private final EDTItem next;

    public ActionChangeRoot(EDTItem prev, EDTItem next) {
        this.prev = prev;
        this.next = next;
    }

    @Override
    public void action(AppContext context) {
        context.setRoot(next);
    }

    @Override
    public void revert(AppContext context) {
        context.setRoot(prev);
    }
}
