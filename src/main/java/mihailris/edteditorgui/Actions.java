package mihailris.edteditorgui;

import java.util.ArrayList;
import java.util.List;

public class Actions {
    private static final List<Action> history = new ArrayList<>();
    private static int pointer;
    public static void act(Action action, AppContext context) {
        while (pointer < history.size())
            history.remove(history.size()-1);
        action.action(context);
        history.add(action);
        pointer++;
        System.out.println("-- le action "+action);
        context.mainFrame.refreshTree();
    }

    public static void undo(AppContext context) {
        if (pointer <= 0)
            return;
        pointer--;
        Action action = history.get(pointer);
        System.out.println("-- undo action "+action);
        action.revert(context);
        context.mainFrame.refreshTree();
    }

    public static void redo(AppContext context) {
        if (pointer == history.size())
            return;
        Action action = history.get(pointer++);
        System.out.println("-- redo action "+action);
        action.action(context);
        context.mainFrame.refreshTree();
    }
}
