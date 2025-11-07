package top.sducraft.mixins.rule.CreeperExplodeLog;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.level.Level;
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

import java.util.ArrayDeque;
import java.util.Deque;

@Mixin(Creeper.class)
public abstract class CreeperMixin extends Mob implements ISelf<Creeper> {

    protected CreeperMixin(EntityType<? extends Mob> entityType, Level level) {
        super(entityType, level);
    }

    @Unique
    private static final Logger LOGGER = LogManager.getLogger("CreeperLog");

    @Unique
    private static final int MAX_POSITION_LOG_TICKS = 600;

    @Unique
    private static final int MAX_DAMAGE_LOG_TICKS = 1000;

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
        if (gameTime % 5 != 0) return;

        ServerPlayer targetPlayer = this.getTarget() instanceof ServerPlayer player ? player : null;
        logPeriodicData(gameTime, targetPlayer);
    }

    @Inject(method = "explodeCreeper", at = @At("HEAD"))
    private void onExplodeCreeper(CallbackInfo ci) {
        if (!SDUcraftCarpetSettings.creeperLog) return;

        Vec3 explosionPos = SduCarpet$self().position();
        LOGGER.info("=== Creeper Explosion Report ===");
        LOGGER.info("Explosion Position: [{}]", formatVec3(explosionPos));

        LOGGER.info("--- Position Tracking (last 30 seconds) ---");
        if (creeperLogs.isEmpty()) {
            LOGGER.info("No position data recorded");
        } else {
            for (CreeperLogHelper.CreeperLogEntry entry : creeperLogs) {
                Vec3 creeperPos = entry.creeperPosition();
                Vec3 playerPos = entry.targetPlayerPosition();

                if (playerPos != null) {
                    LOGGER.info("Time: {} | Target: {} | Creeper Pos: [{}] | Player Pos: [{}]",
                        entry.gameTime(),
                        entry.PlayerName(),
                        formatVec3(creeperPos),
                        formatVec3(playerPos)
                    );
                } else {
                    LOGGER.info("Time: {} | Target: {} | Creeper Pos: [{}] | Player Pos: N/A",
                        entry.gameTime(),
                        entry.PlayerName(),
                        formatVec3(creeperPos)
                    );
                }
            }
        }

        LOGGER.info("--- Damage History (last 50 seconds) ---");
        if (creeperDamageLogs.isEmpty()) {
            LOGGER.info("No damage events recorded");
        } else {
            for (CreeperLogHelper.CreeperDamageLogEntry entry : creeperDamageLogs) {
                Vec3 creeperPos = entry.creeperPosition();
                Vec3 damagePos = entry.damageSourcePosition();

                LOGGER.info("Time: {} | Attacker: {} | Creeper Pos: [{}] | Damage Source Pos: [{}]",
                    entry.gameTime(),
                    entry.PlayerName(),
                    formatVec3(creeperPos),
                    formatVec3(damagePos)
                );
            }
        }

        LOGGER.info("=== End of Report ===");
    }
}
