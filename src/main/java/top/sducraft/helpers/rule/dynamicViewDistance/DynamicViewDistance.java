package top.sducraft.helpers.rule.dynamicViewDistance;

import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.TimeUtil;
import top.sducraft.SDUcraftCarpetSettings;

public class DynamicViewDistance {
    private static int currentVD = 16;
    private static long lastUpdateTime = 0;
    private static final long UPDATE_INTERVAL_MS = 500000;
    private static final int[] thresholds = {30, 40, 50};
    private static final int[] vdLevels = {16, 14, 12, 8};
    private static int tickCounter = 0;

    public static void updateViewDistance(MinecraftServer server) {
        if (!server.isDedicatedServer()) return;
        long now = System.currentTimeMillis();
        if (now - lastUpdateTime < UPDATE_INTERVAL_MS) return;
        double MSPT = ((double) server.getAverageTickTimeNanos()) / TimeUtil.NANOSECONDS_PER_MILLISECOND;
        int newVD = calculateTargetViewDistance(MSPT);

        if (newVD != currentVD) {
            server.getPlayerList().setViewDistance(newVD);
            currentVD = newVD;
            lastUpdateTime = now;
//            for (ServerPlayer player : server.getPlayerList().getPlayers() ) {
//                player.displayClientMessage(Component.literal(String.valueOf(newVD)),false);
//            }
        }
    }

    private static int calculateTargetViewDistance(double mspt) {
        for (int i = 0; i < thresholds.length; i++) {
            if (mspt < thresholds[i]) return vdLevels[i];
        }
        return vdLevels[vdLevels.length - 1];
    }

    public static void onServerTick(MinecraftServer server) {
        if (SDUcraftCarpetSettings.dynamicViewDistance) {
            tickCounter++;
            if (tickCounter > 5) {
                updateViewDistance(server);
                tickCounter = 0;
            }
        }
    }
}
