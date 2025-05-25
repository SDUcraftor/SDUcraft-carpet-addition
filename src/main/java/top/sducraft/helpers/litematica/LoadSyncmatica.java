package top.sducraft.helpers.litematica;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.server.MinecraftServer;
import java.io.File;
import java.io.FileReader;
import java.lang.reflect.Type;
import java.util.*;
import java.util.concurrent.CompletableFuture;

public class LoadSyncmatica {
    public static File configFile;
    public static List<Litematica> litematicas = new ArrayList<>();

    public static class Litematica {
        public UUID id;
        public String file_name;
        public UUID hash;
        public Origin origin;
        public String rotation;
        public String mirror;
        public Owner owner;
        public static class Origin {
            public List<Integer> position;
            public String dimension;
        }
        public static class Owner {
            public UUID uuid;
            public String name;
        }
    }

    public static boolean loadSyncmatica(MinecraftServer server) {
        try {
            File rootDir = server.getServerDirectory();
            File configDir = new File(rootDir, "config/syncmatica");
            configFile = new File(configDir, "placements.json");
            if (!configFile.exists()) {
                configDir.mkdirs();
                return false;
            }
            FileReader reader = new FileReader(configFile);
            Type type = new TypeToken<Map<String, List<Litematica>>>() {
            }.getType();
            Map<String, List<Litematica>> data = new Gson().fromJson(reader, type);
            reader.close();
            litematicas.clear();
            litematicas = data.getOrDefault("placements", new ArrayList<>());
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public static Litematica getLitematica(String name) {

        Litematica litematica = null;
        int count = 0;
        for (Litematica p : litematicas) {
            if (p.file_name.equals(name)) {
                litematica = p;
                count++;
            }
        }

        if (count == 1) {
            return litematica;
        }
        return null;
    }

    public static List<String> fuzzySearch(String keyword) {
        List<String> result = new ArrayList<>();
        for (Litematica p : litematicas) {
            if (p.file_name != null && p.file_name.toLowerCase().contains(keyword.toLowerCase())) {
                result.add(p.file_name);
            }
        }
        return result;
    }

    public static CompletableFuture<Suggestions> suggestFuzzyNames(SuggestionsBuilder builder) {
        String input = builder.getRemaining().toLowerCase(Locale.ROOT);
        List<String> smartSuggestions = new ArrayList<>();
        if (!input.isEmpty()) {
            smartSuggestions = fuzzySearch(input);
        }
        smartSuggestions.forEach(builder::suggest);
        return builder.buildFuture();
    }

}
