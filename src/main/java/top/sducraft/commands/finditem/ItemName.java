package top.sducraft.commands.finditem;

import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import static top.sducraft.helpers.translation.allitem.ItemTranslation.translateItem;

public class ItemName {
    public static final Map<Item,Map.Entry<String,String>> ItemNames = new HashMap<>();

    public static void init() {
        for (Item item : BuiltInRegistries.ITEM){
            String translationKey = item.getDescriptionId();
            ItemNames.put(item,Map.entry(stripPrefix(translationKey),translateItem(translationKey)));
        }
    }

    static String stripPrefix(String key) {
        if (key.startsWith("item.minecraft.")) {
            return key.substring("item.minecraft.".length());
        } else if (key.startsWith("block.minecraft.")) {
            return key.substring("block.minecraft.".length());
        }
        return key;
    }

    public static CompletableFuture<Suggestions> findItemSuggest(SuggestionsBuilder builder) {
        String input = builder.getRemaining().toLowerCase(Locale.ROOT);
        String lowerCaseQuery = input.toLowerCase();

        for (Map.Entry<Item, Map.Entry<String, String>> entry : ItemNames.entrySet()) {
            String englishName = entry.getValue().getKey();
            String chineseName = entry.getValue().getValue();

            if (englishName.toLowerCase().contains(lowerCaseQuery)) {
                builder.suggest(englishName);
            }
            if (chineseName.toLowerCase().contains(lowerCaseQuery)) {
                builder.suggest(chineseName);
            }
        }
        return builder.buildFuture();
    }

    public static Item getItem(String key) {
        for (Map.Entry<Item, Map.Entry<String, String>> entry : ItemNames.entrySet()) {
            String englishName = entry.getValue().getKey();
            String chineseName = entry.getValue().getValue();
            if (englishName.equalsIgnoreCase(key) || chineseName.equalsIgnoreCase(key)) {
                return entry.getKey();
            }
        }
        return null;
    }

}
