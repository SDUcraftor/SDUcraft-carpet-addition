package top.sducraft.helpers.commands.allItemCommand;

import carpet.CarpetServer;
import com.mojang.math.Transformation;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Container;
import net.minecraft.world.entity.Display;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import top.sducraft.config.allItemData.AllItemData;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import static top.sducraft.config.allItemData.AllItemData.dataList;
import static top.sducraft.util.DelayedEventScheduler.addScheduleEvent;

public class SpawnDisplay {
    public static Map<AllItemData.ItemData, DisplayInfo> datadisplayinfoMap = new HashMap<>();

    public static class DisplayInfo {
        public BlockPos displayPos;
        public Item item;

        public DisplayInfo(BlockPos bestCandidate, Item item) {
            this.displayPos = bestCandidate;
            this.item = item;
        }
    }

    public static void spawnItemDisplay(AllItemData.ItemData data , DisplayType type, String tag) {
        ServerLevel level = CarpetServer.minecraft_server.overworld();
        DisplayInfo info = datadisplayinfoMap.get(data);
        spawnItemDisplay(level,info.displayPos,info.item, 0xFFFF00, type, tag );
    }

    public enum DisplayType {
        TEMP,
        PERM
    }

    public static void generateDisplaysInfo(ServerLevel level) {
        datadisplayinfoMap.clear();
        List<BlockPos> referencePositions = new ArrayList<>();
        List<Map.Entry<String, AllItemData.ItemData>> entries = new ArrayList<>(dataList.entrySet());

        for (int i = 0; i < entries.size(); i += 20) {
            AllItemData.ItemData data = entries.get(i).getValue();
            if (data.chestPos == null) continue;

            BlockPos best = null;
            int maxAir = -1;
            for (BlockPos chestPos : data.chestPos) {
                for (Direction dir : Direction.values()) {
                    BlockPos neighbor = chestPos.relative(dir);
                    if (!level.isEmptyBlock(neighbor)) continue;

                    int airCount = countAirInCube7x3x7(level, neighbor);
                    if (airCount > maxAir || (airCount == maxAir && isPreferredDirection(neighbor, best))) {
                        maxAir = airCount;
                        best = neighbor;
                    }
                }
            }
            if (best != null) referencePositions.add(best);
        }

        for (Map.Entry<String, AllItemData.ItemData> entry : dataList.entrySet()) {
            String descriptionId = entry.getKey();
            AllItemData.ItemData data = entry.getValue();
            Item item = getItemByDescriptionId(descriptionId);
            if (item == null || data.chestPos == null) continue;
            BlockPos bestCandidate = null;
            int bestScore = -1;
            for (BlockPos chestPos : data.chestPos) {
                for (Direction dir : Direction.values()) {
                    BlockPos neighbor = chestPos.relative(dir);
                    int score = level.isEmptyBlock(neighbor)? 10 : 0 ;
                    int airCount = countAirInCube7x3x7(level, neighbor);
                    int correlation = calculateCorrelation(neighbor, referencePositions);
                    score += airCount + 3*correlation;
                    if (score > bestScore || (score == bestScore && isPreferredDirection(neighbor, bestCandidate))) {
                        bestScore = score;
                        bestCandidate = neighbor;
                    }
                }
            }
            if (bestCandidate != null) {
                datadisplayinfoMap.put(data, new DisplayInfo(bestCandidate, item));
//                spawnItemDisplay(level, bestCandidate, item, 0xFFFF00);
            }
        }
    }

    private static int calculateCorrelation(BlockPos pos, List<BlockPos> referenceList) {
        int correlation = 0;
        for (BlockPos ref : referenceList) {
            if (pos.getY() == ref.getY()) {
                if( pos.getX() == ref.getX() || pos.getZ() == ref.getZ()) correlation++;
            }
        }
        return correlation;
    }

    private static boolean isPreferredDirection(BlockPos a, BlockPos b) {
        if (b == null) return true;
        if (a.getX() != b.getX()) return a.getX() < b.getX();
        if (a.getY() != b.getY()) return a.getY() < b.getY();
        return a.getZ() < b.getZ();
    }

    private static void spawnItemDisplay(ServerLevel level, BlockPos pos, Item item, int color,DisplayType type,String tag) {
        Display.ItemDisplay display = new Display.ItemDisplay(EntityType.ITEM_DISPLAY, level);
        display.setPos(Vec3.atCenterOf(pos));
        display.setItemStack(new ItemStack(item));
        display.setGlowingTag(true);
        display.addTag(tag);
        display.getEntityData().set(Display.DATA_GLOW_COLOR_OVERRIDE_ID, color);
        Vec3 displayPos = Vec3.atCenterOf(pos);
        Vec3 lookDirection = null;

        for (Direction dir : new Direction[]{Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST}) {
            BlockPos neighborPos = pos.relative(dir);
            BlockEntity be = level.getBlockEntity(neighborPos);
            if (be instanceof Container) {
                Vec3 chestCenter = Vec3.atCenterOf(neighborPos);
                lookDirection = chestCenter.subtract(displayPos).normalize();
                break;
            }
        }

        if (lookDirection != null) {
            Quaternionf rotation = getLookRotation(lookDirection);
            Transformation transform = new Transformation(
                    new Vector3f(0, 0, 0),
                    new Quaternionf(),
                    new Vector3f(0.5f),
                    rotation
            );
            display.setTransformation(transform);
        }
        else {
            display.getEntityData().set(Display.DATA_SCALE_ID, new Vector3f(0.5F));
        }
        level.addFreshEntity(display);
        if (type.equals(DisplayType.TEMP)){
            addScheduleEvent(600 , display::discard);
        }
    }

    private static Quaternionf getLookRotation(Vec3 direction) {
        Vector3f from = new Vector3f(0, 0, -1);
        Vector3f to = new Vector3f((float) direction.x, (float) direction.y, (float) direction.z);
        from.normalize();
        to.normalize();
        return new Quaternionf().rotateTo(from, to);
    }


    private static int countAirInCube7x3x7(ServerLevel level, BlockPos center) {
        int count = 0;
        for (int dx = -3; dx <= 3; dx++) {
            for (int dy = -1; dy <= 1; dy++) {
                for (int dz = -3; dz <= 3; dz++) {
                    BlockPos checkPos = center.offset(dx, dy, dz);
                    if (level.isEmptyBlock(checkPos)) {
                        count++;
                    }
                }
            }
        }
        return count;
    }

    public static void spawnBlockDisplay(ServerLevel level, BlockPos pos, BlockState blockState, int color, String tag) {
        Display.BlockDisplay display = new Display.BlockDisplay(EntityType.BLOCK_DISPLAY, level);
        display.setBlockState(blockState);
        display.setPos(new Vec3(pos.getX(), pos.getY(), pos.getZ()));
        display.setGlowingTag(true);
        display.getEntityData().set(Display.DATA_GLOW_COLOR_OVERRIDE_ID, color);
        display.addTag(tag);
        level.addFreshEntity(display);
    }

    public static Item getItemByDescriptionId(String descriptionId) {
        for (Item item : BuiltInRegistries.ITEM) {
            if (new ItemStack(item).getDescriptionId().equals(descriptionId)) {
                return item;
            }
        }
        return null;
    }

}
