package mihailris.edteditorgui.actions;

import mihailris.edteditorgui.AppContext;
import mihailris.edteditorgui.EDTNodeUserData;
import mihailris.edtfile.EDTGroup;

public class ActionSetValueGroup implements EditorAction {
    private final EDTGroup group;
    private final String key;
    private final Object value;
    private final Object performed;
    private final EDTNodeUserData userData;
    public ActionSetValueGroup(EDTGroup group, String key, Object value, Object performed, EDTNodeUserData userData) {
        this.group = group;
        this.key = key;
        this.value = value;
        this.performed = performed;
        this.userData = userData;
    }

    @Override
    public void action(AppContext context) {
        group.getObjects().put(key, performed);
        userData.setValue(performed);
    }

    @Override
    public void revert(AppContext context) {
        group.getObjects().put(key, value);
        userData.setValue(value);
    }
}
