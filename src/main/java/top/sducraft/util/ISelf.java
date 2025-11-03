package top.sducraft.util;

public interface ISelf<T> {
    @SuppressWarnings("unchecked")
    default T SduCarpet$self() {
        return (T) this;
    }
}
