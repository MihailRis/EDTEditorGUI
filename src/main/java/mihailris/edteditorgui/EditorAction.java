package mihailris.edteditorgui;

public interface EditorAction {
    void action(AppContext context);
    void revert(AppContext context);
}
