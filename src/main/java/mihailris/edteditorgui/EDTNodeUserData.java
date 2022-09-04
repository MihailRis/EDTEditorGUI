package mihailris.edteditorgui;

import mihailris.edtfile.EDTItem;

public class EDTNodeUserData {
    private EDTItem parent;
    private String tag;
    private Object value;
    private boolean editing = true;

    public EDTNodeUserData(EDTItem parent, String tag, Object value) {
        this.parent = parent;
        this.tag = tag;
        this.value = value;
    }

    public EDTItem getParent() {
        return parent;
    }

    public void setParent(EDTItem parent) {
        this.parent = parent;
    }

    public boolean isEditing() {
        return editing;
    }

    public void setEditing(boolean editing) {
        this.editing = editing;
    }

    @Override
    public String toString() {
        if (isEditing())
            return String.valueOf(value);
        return tag;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }

    public int getIndex() {
        return Integer.parseInt(tag);
    }
}
