package mihailris.edteditorgui;

public class EDTNodeUserData {
    private String tag;
    private Object value;

    public EDTNodeUserData(String tag, Object value) {
        this.tag = tag;
        this.value = value;
    }

    @Override
    public String toString() {
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
}
