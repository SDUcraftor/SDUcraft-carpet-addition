//package top.sducraft.helpers.visualizers;
//
//import carpet.CarpetServer;
//import net.minecraft.ChatFormatting;
//import net.minecraft.commands.CommandSourceStack;
//import net.minecraft.core.BlockPos;
//import net.minecraft.nbt.CompoundTag;
//import net.minecraft.nbt.NbtUtils;
//import net.minecraft.server.ServerScoreboard;
//import net.minecraft.server.level.ServerLevel;
//import net.minecraft.world.entity.Display;
//import net.minecraft.world.entity.Entity;
//import net.minecraft.world.entity.EntityType;
//import net.minecraft.world.level.Level;
//import net.minecraft.world.level.block.Blocks;
//import net.minecraft.world.phys.Vec3;
//import net.minecraft.world.scores.PlayerTeam;
//import top.sducraft.SDUcraftCarpetSettings;
//
//import java.util.ArrayList;
//import java.util.List;
//import java.util.Map;
//import java.util.concurrent.ConcurrentHashMap;
//import java.util.function.Predicate;
//
//public class BlockEventVisualizing {
//    public static ConcurrentHashMap<BlockPos, Map.Entry<BlockEventObject, Integer>> visualizers = new ConcurrentHashMap<>();
//    public static int SURVIVE_TIME = 100;
//
//    public static class BlockEventObject {
//
//        public String type;
//        public Display.TextDisplay tickMarker;
//        public Display.BlockDisplay typeMarker;
//
//        public BlockEventObject(ServerLevel world, BlockPos pos, int order) {
//            setVisualizer(world, pos, order);
//        }
//
//        public void setVisualizer(ServerLevel world, BlockPos pos, int order) {
//            if (tickMarker != null && !tickMarker.isRemoved()) {
//                CompoundTag nbt = null;
//                if (typeMarker != null) {
//                    nbt = typeMarker.saveWithoutId(new CompoundTag());
//                }
//                String textJson = "{\"text\":\"" + order + "\",\"color\":\"green\"}";
//                if (nbt != null) {
//                    nbt.remove("text");
//                }
//                nbt.putString("text", textJson);
//                tickMarker.load(nbt);
//            } else {
//                tickMarker = summon(world, pos.getCenter().add(0, -0.4, 0), String.valueOf(order));
//            }
//
//            if (typeMarker == null) {
//                typeMarker = summonMarker(world, pos);
//            }
//        }
//
//        public void removeVisualizer() {
//            if (tickMarker != null) {
//                tickMarker.discard();
//            }
//            if (typeMarker != null) {
//                typeMarker.discard();
//            }
//        }
//
//        public static Display.BlockDisplay summonMarker(Level world, BlockPos pos) {
//            Display.BlockDisplay entity = new Display.BlockDisplay(EntityType.BLOCK_DISPLAY, world);
//            CompoundTag nbt = entity.saveWithoutId(new CompoundTag());
//            nbt.put("block_state", NbtUtils.writeBlockState(Blocks.GREEN_STAINED_GLASS.defaultBlockState()));
//            // 假设 EntityHelper 里的 scaleEntity 方法已转换为 Mojang 映射
//            // nbt = EntityHelper.scaleEntity(nbt, 0.5f);
//            nbt.putInt("glow_color_override", 0xAAFFAA);
//            entity.load(nbt);
//            entity.noPhysics = true;
//            entity.setGlowingTag(true);
//            entity.setPos(pos.getX(), pos.getY(), pos.getZ());
//            entity.addTag("blockEventVisualizer");
//            if (world instanceof ServerLevel serverWorld) {
//                addMarkerToTeam(serverWorld, "blockEventTeam", entity);
//            }
//            entity.setInvisible(true);
//            world.addFreshEntity(entity);
//            return entity;
//        }
//
//        public static Display.TextDisplay summon(ServerLevel world, Vec3 pos, String order) {
//            Display.TextDisplay entity = new Display.TextDisplay(EntityType.TEXT_DISPLAY, world);
//            entity.setInvisible(true);
//            entity.setNoGravity(true);
//            entity.setInvulnerable(true);
//            CompoundTag nbt = entity.saveWithoutId(new CompoundTag());
//            nbt.putString("billboard", "center");
//            String textJson = "{\"text\":\"" + order + "\",\"color\":\"green\"}";
//            nbt.putByte("see_through", (byte) 1);
//            nbt.putInt("background", 0x00000000);
//            nbt.putString("text", textJson);
//            entity.load(nbt);
//            entity.setPos(pos.x(), pos.y(), pos.z());
//            entity.addTag("blockEventVisualizer");
//            world.addFreshEntity(entity);
//            return entity;
//        }
//    }
//
//    public static void setVisualizer(Level world, BlockPos pos, int order) {
//        if (visualizers.containsKey(pos)) {
//            visualizers.put(pos, Map.entry(visualizers.get(pos).getKey(), SURVIVE_TIME));
//        } else {
//            if (world instanceof ServerLevel serverWorld) {
//                visualizers.put(pos, Map.entry(new BlockEventObject(serverWorld, pos, order), SURVIVE_TIME));
//            }
//        }
//
//    }
//
//    private static void addMarkerToTeam(ServerLevel world, String teamName, Display.BlockDisplay marker) {
//        ServerScoreboard scoreboard = world.getScoreboard();
//        PlayerTeam team = scoreboard.getPlayersTeam(teamName);
//        if (team == null) {
//            team = scoreboard.addPlayerTeam(teamName);
//            team.setColor(ChatFormatting.GREEN);
//        }
//        String entityName = marker.getUUID().toString();
//        scoreboard.addPlayerToTeam(entityName, team);
//    }
//
//    public static void updateVisualizer() {
//        if (!SDUcraftCarpetSettings.BlockEventVisualize || !CarpetServer.minecraft_server.tickRateManager().runsNormally())
//            return;
//        visualizers.forEach((pos, entry) -> {
//            BlockEventObject object = entry.getKey();
//            int time = entry.getValue();
//            if (time > 0) {
//                visualizers.put(pos, Map.entry(object, time - 1));
//            } else {
//                object.removeVisualizer();
//                visualizers.remove(pos);
//            }
//        });
//    }
//
//    public static void clearVisualizers(CommandSourceStack source) {
//        visualizers.clear();
//        Predicate<Display.BlockDisplay> predicate = bd -> bd.getTags().contains("blockEventVisualizer");
//        List<Display.BlockDisplay> entities = new ArrayList<>();
//        source.getLevel().getEntities(EntityType.BLOCK_DISPLAY,
//                predicate,
//                entities);
//        entities.forEach(Entity::discard);
//        Predicate<Display.TextDisplay> predicate2 = arm -> arm.getTags().contains("blockEventVisualizer");
//        List<Display.TextDisplay> entities2 = new ArrayList<>();
//        source.getLevel().getEntities(EntityType.TEXT_DISPLAY,
//                predicate2,
//                entities2);
//        entities2.forEach(Entity::discard);
//    }
//}
//
