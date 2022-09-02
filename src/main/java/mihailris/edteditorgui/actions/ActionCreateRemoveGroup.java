package mihailris.edteditorgui.actions;

import mihailris.edteditorgui.Action;
import mihailris.edteditorgui.AppContext;
import mihailris.edtfile.EDTGroup;

public class ActionCreateRemoveGroup implements Action {
    private final EDTGroup group;
    private final String tag;
    private final Object value;
    private final boolean add;

    public ActionCreateRemoveGroup(EDTGroup group, String tag, Object value, boolean add) {
        this.group = group;
        this.tag = tag;
        this.value = value;
        this.add = add;
    }

    private void add(){
        group.getObjects().put(tag, value);
    }

    private void remove(){
        group.getObjects().remove(tag);
    }

    @Override
    public void action(AppContext context) {
        if (add)
            add();
        else
            remove();
    }

    @Override
    public void revert(AppContext context) {
        if (!add)
            add();
        else
            remove();
    }
}
