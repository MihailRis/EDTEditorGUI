package mihailris.edteditorgui.actions;

import mihailris.edteditorgui.AppContext;

import java.util.ArrayList;
import java.util.List;

public class Actions {
    private static final List<EditorAction> history = new ArrayList<>();
    private static int pointer;
    private static EditorAction lastSaved;

    public static void act(EditorAction action, AppContext context) {
        while (pointer < history.size())
            history.remove(history.size()-1);
        history.add(action);
        pointer++;
        action.action(context);
        context.mainFrame.onSomethingChanged();
    }

    public static void undo(AppContext context) {
        if (pointer <= 0)
            return;
        pointer--;
        EditorAction action = history.get(pointer);
        action.revert(context);
        context.mainFrame.onSomethingChanged();
    }

    public static void redo(AppContext context) {
        if (pointer == history.size())
            return;
        EditorAction action = history.get(pointer++);
        action.action(context);
        context.mainFrame.onSomethingChanged();
    }

    public static boolean isAllSaved(){
        if (lastSaved == null)
            return pointer == 0;
        if (pointer == 0)
            return true;
        return history.get(pointer-1) == lastSaved;
    }

    public static void save(){
        if (pointer == 0)
            return;
        lastSaved = history.get(pointer-1);
    }
}
