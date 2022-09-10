package mihailris.edteditorgui.actions;

import mihailris.edteditorgui.AppContext;
import mihailris.edtfile.EDTItem;

public class ActionRenameRoot implements EditorAction {
    private final EDTItem root;
    private final String prevName;
    private final String nextName;

    public ActionRenameRoot(EDTItem root, String prevName, String nextName) {
        this.root = root;
        this.prevName = prevName;
        this.nextName = nextName;
    }

    @Override
    public void action(AppContext context) {
        root.setTag(nextName);
    }

    @Override
    public void revert(AppContext context) {
        root.setTag(prevName);
    }
}
