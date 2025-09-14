package top.sducraft.commands.finditem;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class FindItemResultCache {
    private static final Map<UUID, Map<UUID, List<FindItemCommand.MergedResult>>> CACHE = new HashMap<>();

    public static UUID cacheGroup(UUID playerUuid, List<FindItemCommand.MergedResult> group) {
        CACHE.computeIfAbsent(playerUuid, k -> new HashMap<>());
        UUID groupId = UUID.randomUUID();
        CACHE.get(playerUuid).put(groupId, group);
        return groupId;
    }

    public static List<FindItemCommand.MergedResult> getGroup(UUID playerUuid, UUID groupId) {
        Map<UUID, List<FindItemCommand.MergedResult>> playerCache = CACHE.get(playerUuid);
        return playerCache != null ? playerCache.get(groupId) : null;
    }

    public static void clear(UUID playerUuid) {
        CACHE.remove(playerUuid);
    }
}