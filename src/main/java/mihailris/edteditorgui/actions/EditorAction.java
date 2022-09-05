package mihailris.edteditorgui.actions;

import mihailris.edteditorgui.AppContext;

public interface EditorAction {
    void action(AppContext context);
    void revert(AppContext context);
}
