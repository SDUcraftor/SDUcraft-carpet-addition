package top.sducraft.commands.finditem;

import net.minecraft.world.item.Item;

import java.util.HashSet;
import java.util.Set;

import static top.sducraft.commands.finditem.ItemName.stripPrefix;
import static top.sducraft.helpers.translation.allitem.ItemTranslation.translateItem;

public class FilterParser {
    public record Filter(Set<String> included, Set<String> excluded) {
        public boolean matches(String name) {
            if (name == null) return false;
            if (!included.isEmpty() && !included.contains(name)) {
                return false;
            }
            return !(excluded.contains(name));
        }

        public boolean itemMatches(Item item) {
            if (item == null) return false;
            if (!included.isEmpty() && !included.contains(stripPrefix(item.getDescriptionId())) && !included.contains(translateItem(item.getDescriptionId()))) {
                return false;
            }
            return !(excluded.contains(stripPrefix(item.getDescriptionId())) || excluded.contains(translateItem(item.getDescriptionId())));
        }
    }

    public static Filter parse(String filterString) {
        Set<String> included = new HashSet<>();
        Set<String> excluded = new HashSet<>();

        if (filterString == null || filterString.isBlank() || filterString.equals("*")) {
            return new Filter(included, excluded); // An empty filter matches everything
        }

        String[] parts = filterString.split("&");
        for (String part : parts) {
            part = part.trim();
            if (part.isEmpty()) continue;

            if (part.startsWith("!=")) {
                excluded.add(part.substring(2).trim());
            } else {
                included.add(part.trim());
            }
        }
        return new Filter(included, excluded);
    }
}