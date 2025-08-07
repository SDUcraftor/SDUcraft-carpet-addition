package top.sducraft.util.dialog;

import com.google.gson.JsonObject;

public class ComponentFactory {

    /**
     * Creates a simple text component.
     * @param text The text to display.
     * @return A JsonObject representing the text component.
     */
    public static JsonObject createText(String text) {
        JsonObject obj = new JsonObject();
        obj.addProperty("text", text);
        return obj;
    }

    /**
     * Creates a translatable text component.
     * @param translateKey The translation key (e.g., "gui.cancel").
     * @return A JsonObject representing the translatable component.
     */
    public static JsonObject createTranslated(String translateKey) {
        JsonObject obj = new JsonObject();
        obj.addProperty("translate", translateKey);
        return obj;
    }
}
