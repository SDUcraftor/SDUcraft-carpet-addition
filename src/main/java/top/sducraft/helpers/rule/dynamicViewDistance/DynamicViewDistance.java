package top.sducraft.helpers.rule.dynamicViewDistance;

import carpet.CarpetServer;
import carpet.patches.EntityPlayerMPFake;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.TimeUtil;
import top.sducraft.SDUcraftCarpetSettings;

public class DynamicViewDistance {
    private static int currentVD = 16;
    private static long lastUpdateTime = 0;

    private static final long UPDATE_INTERVAL_MS = 100000;

    private static final int[] thresholds = {25, 40};
    private static final int[] vdLevels = {14, 10, 8};

    private static final int HYSTERESIS_MS = 10;      // 滞回余量
    private static final double EMA_ALPHA = 0.2;     // 平滑系数（0-1，越小越平滑）
    private static double smoothedMspt = -1;

    private static int tickCounter = 0;
    private static int playerCounter = 0;

    public static void updateViewDistance(MinecraftServer server) {
        if (!server.isDedicatedServer()) return;

        long now = System.currentTimeMillis();
        if (now - lastUpdateTime < UPDATE_INTERVAL_MS) return;

        // 使用平滑后的 MSPT，若尚未初始化则退回即时值
        double instantMspt = ((double) server.getAverageTickTimeNanos()) / TimeUtil.NANOSECONDS_PER_MILLISECOND;
        double mspt = (smoothedMspt > 0) ? smoothedMspt : instantMspt;

        int newVD = calculateTargetViewDistance(mspt);

        if (newVD != currentVD) {
            server.getPlayerList().setViewDistance(newVD);
            currentVD = newVD;
            lastUpdateTime = now;
            for (ServerPlayer player : server.getPlayerList().getPlayers()) {
                if (!player.hasPermissions(2)) continue;
                player.displayClientMessage(Component.literal("VD -> " + newVD + " (MSPT~" + String.format("%.1f", mspt) + ")"), false);
            }
        }
    }

    private static int calculateTargetViewDistance(double mspt) {
        playerCounter = 0;
        for (ServerPlayer player : CarpetServer.minecraft_server.getPlayerList().getPlayers()) {
            if (!(player instanceof EntityPlayerMPFake)) {
                playerCounter++;
            }
            if (playerCounter >= 15) {
                return 8;
            }
        }


        int currentIdx = indexOfVD(currentVD);
        if (currentIdx == -1) {
            for (int i = 0; i < thresholds.length; i++) {
                if (mspt < thresholds[i]) return vdLevels[i];
            }
            return vdLevels[vdLevels.length - 1];
        }

        if (currentIdx < vdLevels.length - 1) {
            int upper = thresholds[currentIdx];
            if (mspt >= upper) {
                return vdLevels[currentIdx + 1];
            }
        }

        if (currentIdx > 0) {
            int lower = thresholds[currentIdx - 1];
            if (mspt <= lower - 5) {
                return vdLevels[currentIdx - 1];
            }
        }

        return vdLevels[currentIdx];
    }

    private static int indexOfVD(int vd) {
        for (int i = 0; i < vdLevels.length; i++) {
            if (vdLevels[i] == vd) return i;
        }
        return -1;
    }

    public static void onServerTick(MinecraftServer server) {
        if (!SDUcraftCarpetSettings.dynamicViewDistance) return;
        if (tickCounter >= 10) {
            double instantMspt = ((double) server.getAverageTickTimeNanos()) / TimeUtil.NANOSECONDS_PER_MILLISECOND;
            if (smoothedMspt < 0) {
                smoothedMspt = instantMspt;
            } else {
                smoothedMspt = EMA_ALPHA * instantMspt + (1.0 - EMA_ALPHA) * smoothedMspt;
            }

            updateViewDistance(server);
            tickCounter = 0;
        }
        tickCounter++;
    }
}