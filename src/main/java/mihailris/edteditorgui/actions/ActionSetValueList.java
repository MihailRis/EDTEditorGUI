package mihailris.edteditorgui.actions;

import mihailris.edteditorgui.AppContext;
import mihailris.edteditorgui.EDTNodeUserData;
import mihailris.edtfile.EDTList;

public class ActionSetValueList implements EditorAction {
    private final EDTList list;
    private final int index;
    private final Object value;
    private final Object performed;
    private final EDTNodeUserData userData;
    public ActionSetValueList(EDTList list, int index, Object value, Object performed, EDTNodeUserData userData) {
        this.list = list;
        this.index = index;
        this.value = value;
        this.performed = performed;
        this.userData = userData;
    }

    @Override
    public void action(AppContext context) {
        list.getObjects().set(index, performed);
        userData.setValue(performed);
    }

    @Override
    public void revert(AppContext context) {
        list.getObjects().set(index, value);
        userData.setValue(value);
    }
}
