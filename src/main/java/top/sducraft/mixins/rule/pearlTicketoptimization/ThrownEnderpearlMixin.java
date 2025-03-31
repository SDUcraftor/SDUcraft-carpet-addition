package top.sducraft.mixins.rule.pearlTicketoptimization;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.entity.projectile.ThrownEnderpearl;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import top.sducraft.SDUcraftCarpetSettings;
import static net.minecraft.server.level.FullChunkStatus.ENTITY_TICKING;
import static top.sducraft.helpers.rule.chunkLoadHelper.RegistTicket.addEndPearlTicket;

@Mixin(ThrownEnderpearl.class)
public abstract class ThrownEnderpearlMixin extends ThrowableItemProjectile {

    private Vec3 realVelocity;
    private Vec3 realPos;
    private boolean sync = true;

    public ThrownEnderpearlMixin(EntityType<? extends ThrowableItemProjectile> entityType, Level level) {
        super(entityType, level);
    }

    @Unique
    private static int getHighestBlock(CompoundTag compoundTag) {
        int highestY = Integer.MIN_VALUE;
        if (compoundTag != null) {
            for (long element : compoundTag.getCompound("Heightmaps").getLongArray("MOTION_BLOCKING")) {
                for (int i = 0; i < 7; i++) {
                    int y = (int) (element & 0b111111111) - 1;
                    if (y > highestY) highestY = y;
                    element = element >> 9;
                }
            }
        }
        return highestY;
    }

    private static boolean isEntityTickingChunk(LevelChunk chunk) {
        return (chunk != null && chunk.getFullStatus() == ENTITY_TICKING);
    }

    @Inject(method = "tick", at = @At("HEAD"))
    public void tick(CallbackInfo ci) {
        Level level = this.getCommandSenderWorld();
        if (level instanceof ServerLevel) {
            Vec3 currVelocity = this.getDeltaMovement().add(Vec3.ZERO);
            Vec3 currPos = this.position().add(Vec3.ZERO);

            if (((Math.abs(currVelocity.length()) >= 10)||!sync) &&SDUcraftCarpetSettings.pearlTicketoptimization) {
//          同步珍珠位置
                if (sync) {
                    realPos = this.position().add(Vec3.ZERO);
                    realVelocity = this.getDeltaMovement().add(Vec3.ZERO);
                }

                Vec3 nextPos = this.realPos.add(this.realVelocity);
                Vec3 nextVelocity = this.realVelocity.scale(0.99).subtract(0, this.getGravity(), 0);

                ChunkPos currChunk = new ChunkPos(new BlockPos((int) currPos.x, (int) currPos.y, (int) currPos.z));
                ChunkPos nextChunk = new ChunkPos(new BlockPos((int) nextPos.x, (int) nextPos.y, (int) nextPos.z));


                ServerChunkCache serverChunkSource = ((ServerLevel) level).getChunkSource();
                LevelChunk levelChunk = serverChunkSource.getChunkNow(nextChunk.x, nextChunk.z);

                if(levelChunk!=null&&(!sync || !isEntityTickingChunk(levelChunk))) {
                    int highestY;
                    try {
                        CompoundTag currcompoundTag = serverChunkSource.chunkMap.read(new ChunkPos(currChunk.x, currChunk.z)).get().orElse(null);
                        highestY = getHighestBlock(currcompoundTag);
                        for (int i = -1; i <= 1; i++) {
                            for (int j = -1; j <= 1; j++) {
                                CompoundTag nextcompoundTag = serverChunkSource.chunkMap.read(new ChunkPos(nextChunk.x + i, nextChunk.z + j)).get().orElse(null);
                                if (getHighestBlock(nextcompoundTag) > highestY) {
                                    highestY = getHighestBlock(nextcompoundTag);
                                }
                            }
                        }
                    } catch (Exception e) {
                        throw new RuntimeException("NbtCompound exception");
                    }

                    ServerLevel serverLevel = (ServerLevel) this.level();
                    int minY = serverLevel.getMinBuildHeight();
                    highestY += minY;

                    if (realPos.y > highestY && nextPos.y > highestY && nextPos.y + nextVelocity.y > highestY) {
                        this.setPos(currPos);
                        this.setDeltaMovement(Vec3.ZERO);
                        sync = false;
                        addEndPearlTicket((ServerLevel) this.level(), currChunk);
                    } else {
                        this.setPos(realPos);
                        this.setDeltaMovement(realVelocity);
                        sync = true;
                        addEndPearlTicket((ServerLevel) this.level(), nextChunk);
                    }
                }

//                if(pearlLog) {
//                    PlayerList playerList = server.getPlayerList();
//                    for (ServerPlayer player : playerList.getPlayers()) {
//                        if (player != null) {
//                            player.displayClientMessage(Component.literal("\ncurrPos:" + currPos + "\ncurrVelocity:" + currVelocity + "\nrealPos:" + realPos.toString() + "\nrealVelocity:" + realVelocity.toString() + "\nnextPos" + nextPos + "\nnextVelocity " + nextVelocity + sync), false);
//                        }
//                    }
//                }
                realPos = nextPos;
                realVelocity = nextVelocity;
            }
        }
    }
}
