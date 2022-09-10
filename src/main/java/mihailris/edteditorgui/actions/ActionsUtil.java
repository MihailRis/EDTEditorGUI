package mihailris.edteditorgui.actions;

import mihailris.edteditorgui.AppContext;
import mihailris.edteditorgui.EDTNodeUserData;
import mihailris.edtfile.EDTGroup;
import mihailris.edtfile.EDTItem;
import mihailris.edtfile.EDTList;

public class ActionsUtil {
    public static void actionSetValue(EDTNodeUserData userData, Object performed, AppContext context){
        EDTItem parent = userData.getParent();
        if (parent instanceof EDTGroup) {
            Actions.act(
                    new ActionSetValueGroup(
                            (EDTGroup) userData.getParent(),
                            userData.getTag(),
                            userData.getValue(),
                            performed,
                            userData),
                    context);
        } else if  (parent instanceof EDTList) {
            Actions.act(
                    new ActionSetValueList(
                            (EDTList) userData.getParent(),
                            userData.getIndex(),
                            userData.getValue(),
                            performed,
                            userData),
                    context);
        } else {
            Actions.act(new ActionChangeRoot((EDTItem) userData.getValue(), (EDTItem) performed), context);
        }
    }
}
