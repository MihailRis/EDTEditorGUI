package mihailris.edteditorgui.actions;

import mihailris.edteditorgui.EditorAction;
import mihailris.edteditorgui.AppContext;
import mihailris.edtfile.EDTList;

public class ActionCreateRemoveList implements EditorAction {
    private final EDTList list;
    private final int index;
    private final Object value;
    private final boolean add;
    public ActionCreateRemoveList(EDTList list, int index, Object object, boolean add) {
        this.list = list;
        this.index = index;
        this.value = object;
        this.add = add;
    }

    @Override
    public void action(AppContext context) {
        if (add)
            list.getObjects().add(index, value);
        else
            list.getObjects().remove(index);
    }

    @Override
    public void revert(AppContext context) {
        if (add)
            list.getObjects().remove(index);
        else
            list.getObjects().add(index);
    }
}
