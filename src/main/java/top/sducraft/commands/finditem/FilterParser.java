package top.sducraft.commands.finditem;

import java.util.HashSet;
import java.util.Set;

public class FilterParser {
    public record Filter(Set<String> included, Set<String> excluded) {
        public boolean matches(String name) {
            if (name == null) return false;
            // If there are inclusion rules, it must match one of them.
            if (!included.isEmpty() && !included.contains(name)) {
                return false;
            }
            // It must not match any exclusion rules.
            return !excluded.contains(name);
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