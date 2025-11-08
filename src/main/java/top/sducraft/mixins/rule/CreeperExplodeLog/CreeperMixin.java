package top.sducraft.mixins.rule.CreeperExplodeLog;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.LevelResource;
import net.minecraft.world.phys.Vec3;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import top.sducraft.SDUcraftCarpetSettings;
import top.sducraft.helpers.rule.CreeperLogHelper.CreeperLogHelper;
import top.sducraft.util.ISelf;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayDeque;
import java.util.Date;
import java.util.Deque;

@Mixin(Creeper.class)
public abstract class CreeperMixin extends Mob implements ISelf<Creeper> {

    protected CreeperMixin(EntityType<? extends Mob> entityType, Level level) {
        super(entityType, level);
    }

    @Unique
    private static final Logger LOGGER = LogManager.getLogger("CreeperLog");

    @Unique
    private static final int MAX_POSITION_LOG_TICKS = 1200;

    @Unique
    private static final int MAX_DAMAGE_LOG_TICKS = 1200;

    @Unique
    private final Deque<CreeperLogHelper.CreeperLogEntry> creeperLogs = new ArrayDeque<>();

    @Unique
    private final Deque<CreeperLogHelper.CreeperDamageLogEntry> creeperDamageLogs = new ArrayDeque<>();

    @Unique
    private boolean startLog = false;

    @Unique
    private static String formatVec3(Vec3 vec) {
        return String.format("%.2f, %.2f, %.2f", vec.x, vec.y, vec.z);
    }

    @Unique
    private void saveExplosionToFile(MinecraftServer server, Vec3 explosionPos, long gameTime) {
        try {
            File logDir = server.getWorldPath(LevelResource.ROOT).resolve("data").resolve("creeperlog").toFile();
            if (!logDir.exists()) {
                logDir.mkdirs();
            }

            String timestamp = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss").format(new Date());
            String fileName = String.format("%s-[%.0f,%.0f,%.0f].log",
                    timestamp, explosionPos.x, explosionPos.y, explosionPos.z);
            File logFile = new File(logDir, fileName);

            try (PrintWriter writer = new PrintWriter(new FileWriter(logFile))) {
                writer.println("=== Creeper Explosion Report ===");
                writer.println("Explosion Position: [" + formatVec3(explosionPos) + "]");
                writer.println("Game Time: " + gameTime);
                writer.println("Real Time: " + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
                writer.println();

                writer.println("--- Position Tracking (last 30 seconds) ---");
                if (creeperLogs.isEmpty()) {
                    writer.println("No position data recorded");
                } else {
                    for (CreeperLogHelper.CreeperLogEntry entry : creeperLogs) {
                        Vec3 creeperPos = entry.creeperPosition();
                        Vec3 playerPos = entry.targetPlayerPosition();

                        if (playerPos != null) {
                            writer.println(String.format("Time: %d | Target: %s | Creeper Pos: [%s] | Player Pos: [%s]",
                                    entry.gameTime(),
                                    entry.PlayerName(),
                                    formatVec3(creeperPos),
                                    formatVec3(playerPos)
                            ));
                        } else {
                            writer.println(String.format("Time: %d | Target: %s | Creeper Pos: [%s] | Player Pos: N/A",
                                    entry.gameTime(),
                                    entry.PlayerName(),
                                    formatVec3(creeperPos)
                            ));
                        }
                    }
                }
                writer.println();

                writer.println("--- Damage History (last 50 seconds) ---");
                if (creeperDamageLogs.isEmpty()) {
                    writer.println("No damage events recorded");
                } else {
                    for (CreeperLogHelper.CreeperDamageLogEntry entry : creeperDamageLogs) {
                        Vec3 creeperPos = entry.creeperPosition();
                        Vec3 damagePos = entry.damageSourcePosition();

                        writer.println(String.format("Time: %d | Attacker: %s | Creeper Pos: [%s] | Damage Source Pos: [%s]",
                                entry.gameTime(),
                                entry.PlayerName(),
                                formatVec3(creeperPos),
                                formatVec3(damagePos)
                        ));
                    }
                }
                writer.println();
                writer.println("=== End of Report ===");
            }

            LOGGER.info("Creeper explosion log saved to: {}", logFile.getAbsolutePath());
        } catch (IOException e) {
            LOGGER.error("Failed to save creeper explosion log", e);
        }
    }

    @Unique
    private void logPeriodicData(long gameTime, ServerPlayer targetPlayer) {
        String playerName = targetPlayer != null ? targetPlayer.getName().getString() : "No Target";
        Vec3 playerPosition = targetPlayer != null ? targetPlayer.position() : null;
        creeperLogs.addLast(new CreeperLogHelper.CreeperLogEntry(gameTime, playerName, SduCarpet$self().position(), playerPosition));

        while (!creeperLogs.isEmpty() && gameTime - creeperLogs.peekFirst().gameTime() > MAX_POSITION_LOG_TICKS) {
            creeperLogs.removeFirst();
        }
    }

    @Override
    public boolean hurtServer(ServerLevel serverLevel, DamageSource damageSource, float f) {
        if (SDUcraftCarpetSettings.creeperLog && damageSource.getEntity() instanceof ServerPlayer player) {
            long gameTime = serverLevel.getGameTime();

            creeperDamageLogs.addLast(new CreeperLogHelper.CreeperDamageLogEntry(gameTime, player.getName().getString(), SduCarpet$self().position(), damageSource.getSourcePosition()));

            while (!creeperDamageLogs.isEmpty() && gameTime - creeperDamageLogs.peekFirst().gameTime() > MAX_DAMAGE_LOG_TICKS) {
                creeperDamageLogs.removeFirst();
            }
        }
        return super.hurtServer(serverLevel, damageSource, f);
    }

    @Inject(method = "tick", at = @At("HEAD"))
    private void tick(CallbackInfo ci) {
        if (!SDUcraftCarpetSettings.creeperLog) return;
        if (!(this.level() instanceof ServerLevel serverLevel)) return;
        if (!startLog && this.getTarget() != null) {
            startLog = true;
        }

        if (!startLog) return;

        long gameTime = serverLevel.getGameTime();
        if (gameTime % 10 != 0) return;

        ServerPlayer targetPlayer = this.getTarget() instanceof ServerPlayer player ? player : null;
        logPeriodicData(gameTime, targetPlayer);
    }

    @Inject(method = "explodeCreeper", at = @At("HEAD"))
    private void onExplodeCreeper(CallbackInfo ci) {
        if (!SDUcraftCarpetSettings.creeperLog) return;
        if (!(this.level() instanceof ServerLevel serverLevel)) return;

        Vec3 explosionPos = SduCarpet$self().position();
        long gameTime = serverLevel.getGameTime();
        MinecraftServer server = serverLevel.getServer();

        saveExplosionToFile(server, explosionPos, gameTime);
    }
}
