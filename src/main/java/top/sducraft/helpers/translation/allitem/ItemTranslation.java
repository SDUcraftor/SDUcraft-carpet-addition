package top.sducraft.helpers.translation.allitem;

import java.util.Map;
import static carpet.utils.Translations.getTranslationFromResourcePath;

public class ItemTranslation {

    public static final Map<String, String> translations = getTranslationFromResourcePath("assets/sdu/lang/item_zh_cn.json");
    public static String translateItem(String key) {
        return translations.getOrDefault(key, key);
    }
}
