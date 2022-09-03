package mihailris.edteditorgui;

import java.util.ArrayList;
import java.util.List;

public class Actions {
    private static final List<EditorAction> history = new ArrayList<>();
    private static int pointer;
    public static void act(EditorAction action, AppContext context) {
        while (pointer < history.size())
            history.remove(history.size()-1);
        action.action(context);
        history.add(action);
        pointer++;
        System.out.println("-- le action "+action);
        context.mainFrame.refreshTree();
    }

    public static void undo(AppContext context) {
        System.out.println("Actions.undo");
        if (pointer <= 0)
            return;
        pointer--;
        EditorAction action = history.get(pointer);
        System.out.println("-- undo action "+action);
        action.revert(context);
        context.mainFrame.refreshTree();
    }

    public static void redo(AppContext context) {
        System.out.println("Actions.redo");
        if (pointer == history.size())
            return;
        EditorAction action = history.get(pointer++);
        System.out.println("-- redo action "+action);
        action.action(context);
        context.mainFrame.refreshTree();
    }
}
