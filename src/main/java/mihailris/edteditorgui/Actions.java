package mihailris.edteditorgui;

public class Actions {
    public static void act(Action action, AppContext context) {
        action.action(context);
    }
}
