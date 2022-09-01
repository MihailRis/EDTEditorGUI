package mihailris.edteditorgui;

public interface Action {
    void action(AppContext context);
    void revert(AppContext context);
}
