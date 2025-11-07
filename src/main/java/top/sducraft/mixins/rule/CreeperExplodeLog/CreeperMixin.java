package top.sducraft.mixins.rule.CreeperExplodeLog;

import carpet.CarpetServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import top.sducraft.SDUcraftCarpetSettings;
import top.sducraft.helpers.rule.CreeperLogHelper.CreeperLogHelper;
import top.sducraft.util.ISelf;

import java.util.ArrayList;
import java.util.List;

@Mixin(Creeper.class)
public abstract class CreeperMixin extends Mob implements ISelf<Creeper> {

    protected CreeperMixin(EntityType<? extends Mob> entityType, Level level) {
        super(entityType, level);
    }

    @Unique
    private final static Logger LOGGER = LogManager.getLogger("CreeperLog");

    @Unique
    private final List<CreeperLogHelper.CreeperLogEntry> creeperLogs = new ArrayList<>();
    @Unique
    private final List<CreeperLogHelper.CreeperDamageLogEntry> creeperDamageLogs = new ArrayList<>();

    @Unique
    public void logPeriodicData(long gameTime, Creeper creeper,@Nullable ServerPlayer targetPlayer) {
        if (targetPlayer != null) {
            creeperLogs.add(new CreeperLogHelper.CreeperLogEntry(gameTime, targetPlayer.getName().getString(), creeper.position(), targetPlayer.position()));
        }
        else {
            creeperLogs.add(new CreeperLogHelper.CreeperLogEntry(gameTime, "No Target", creeper.position(),null));
        }
        creeperLogs.removeIf(entry -> gameTime - entry.gameTime() > 400);
    }

    @Unique
    private boolean startLog = false;

    @Override
    public boolean hurtServer(ServerLevel serverLevel, DamageSource damageSource, float f) {
        if (SDUcraftCarpetSettings.creeperLog && damageSource.getEntity() instanceof ServerPlayer player) {
            creeperDamageLogs.removeIf(entry -> serverLevel.getGameTime() - entry.gameTime() > 1000);
            creeperDamageLogs.add(new CreeperLogHelper.CreeperDamageLogEntry(serverLevel.getGameTime(), player.getName().getString(), SduCarpet$self().position(), player.position()));
        }
        return super.hurtServer(serverLevel, damageSource, f);
    }

    @Inject(method = "tick", at = @At("HEAD"))
    private void tick(CallbackInfo ci) {
        if (SDUcraftCarpetSettings.creeperLog) {
            if (!startLog && this.getTarget() != null) {
                startLog = true;
            }
            long gameTime = CarpetServer.minecraft_server.overworld().getGameTime();
            if (!startLog || gameTime % 5 !=0) return;
            if (this.getTarget() instanceof ServerPlayer player) {
                logPeriodicData(CarpetServer.minecraft_server.overworld().getGameTime(), SduCarpet$self(), player);
            }
            else {
                    logPeriodicData(CarpetServer.minecraft_server.overworld().getGameTime(), SduCarpet$self(), null);
            }

        }
    }

    @Inject(method = "explodeCreeper", at = @At("HEAD"))
    private void onExplodeCreeper(CallbackInfo ci) {
        if(!SDUcraftCarpetSettings.creeperLog) return;

        LOGGER.info("=== Creeper Explosion Report ===");
        LOGGER.info("Explosion Position: {}", SduCarpet$self().position());

        LOGGER.info("--- Position Tracking (last 20 seconds) ---");
        if (creeperLogs.isEmpty()) {
            LOGGER.info("No position data recorded");
        } else {
            creeperLogs.stream()
                .sorted((a, b) -> Long.compare(a.gameTime(), b.gameTime()))
                .forEach(entry -> {
                    if (entry.targetPlayerPosition() != null) {
                        LOGGER.info("Time: {} | Target: {} | Creeper Pos: [{}, {}, {}] | Player Pos: [{}, {}, {}]",
                            entry.gameTime(),
                            entry.PlayerName(),
                            String.format("%.2f", entry.creeperPosition().x),
                            String.format("%.2f", entry.creeperPosition().y),
                            String.format("%.2f", entry.creeperPosition().z),
                            String.format("%.2f", entry.targetPlayerPosition().x),
                            String.format("%.2f", entry.targetPlayerPosition().y),
                            String.format("%.2f", entry.targetPlayerPosition().z)
                        );
                    }
                    else {
                        LOGGER.info("Time: {} | Target: {} | Creeper Pos: [{}, {}, {}] | Player Pos: N/A",
                            entry.gameTime(),
                            entry.PlayerName(),
                            String.format("%.2f", entry.creeperPosition().x),
                            String.format("%.2f", entry.creeperPosition().y),
                            String.format("%.2f", entry.creeperPosition().z)
                        );
                    }
                });
        }

        LOGGER.info("--- Damage History (last 20 seconds) ---");
        if (creeperDamageLogs.isEmpty()) {
            LOGGER.info("No damage events recorded");
        } else {
            creeperDamageLogs.stream()
                .sorted((a, b) -> Long.compare(a.gameTime(), b.gameTime()))
                .forEach(entry -> {
                    LOGGER.info("Time: {} | Attacker: {} | Creeper Pos: [{}, {}, {}] | Damage Source Pos: [{}, {}, {}]",
                        entry.gameTime(),
                        entry.PlayerName(),
                        String.format("%.2f", entry.creeperPosition().x),
                        String.format("%.2f", entry.creeperPosition().y),
                        String.format("%.2f", entry.creeperPosition().z),
                        String.format("%.2f", entry.damageSourcePosition().x),
                        String.format("%.2f", entry.damageSourcePosition().y),
                        String.format("%.2f", entry.damageSourcePosition().z)
                    );
                });
        }

        LOGGER.info("=== End of Report ===");
    }
}
