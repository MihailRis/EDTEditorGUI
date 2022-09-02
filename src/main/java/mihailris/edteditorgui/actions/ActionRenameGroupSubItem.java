package mihailris.edteditorgui.actions;

import mihailris.edteditorgui.Action;
import mihailris.edteditorgui.AppContext;
import mihailris.edtfile.EDTGroup;
import mihailris.edtfile.EDTItem;

public class ActionRenameGroupSubItem implements Action {
    private final EDTGroup selectionParent;

    private final String prevName;
    private final String nextName;

    public ActionRenameGroupSubItem(
            EDTGroup selectionParent,
            String prevName,
            String nextName) {
        this.selectionParent = selectionParent;
        this.prevName = prevName;
        this.nextName = nextName;
    }

    @Override
    public void action(AppContext context) {
        Object value = selectionParent.getObjects().get(prevName);
        selectionParent.getObjects().remove(prevName);
        selectionParent.getObjects().put(nextName, value);
        if (value instanceof EDTItem){
            ((EDTItem) value).setTag(nextName);
        }
    }

    @Override
    public void revert(AppContext context) {
        Object value = selectionParent.getObjects().get(nextName);
        selectionParent.getObjects().remove(nextName);
        selectionParent.getObjects().put(prevName, value);
        if (value instanceof EDTItem){
            ((EDTItem) value).setTag(prevName);
        }
    }
}
