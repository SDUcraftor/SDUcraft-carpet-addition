package top.sducraft.helpers.commands.allItemCommand;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.HopperBlockEntity;
import java.util.HashSet;
import java.util.Objects;
import static carpet.utils.Translations.tr;
import static top.sducraft.config.allItemData.AllItemData.addItem;
import static top.sducraft.config.allItemData.AllItemData.delItem;

public class AllItemCommandHelper {
    public static int addItemToData(String type,BlockPos startPos,BlockPos endPos,BlockPos startStorePos,BlockPos endStorePos,BlockPos startChestPos,BlockPos endChestPos ,CommandSourceStack sourceStack){
        if(!Objects.equals(type, "item") && !Objects.equals(type, "bulk")) {
            sourceStack.sendFailure(Component.literal(tr("sducarpet.easycommand.allitemcommand")));
            return 0;
        }
        else {
            int dx = Integer.compare(endPos.getX(), startPos.getX());
            int dz = Integer.compare(endPos.getZ(), startPos.getZ());
            if ((dx != 0 && dz != 0) || (dx == 0 && dz == 0)) {
                sourceStack.sendFailure(Component.literal(tr("sducarpet.easycommand.allitemcommand1")));
                return 0;
            }
            BlockPos current = startPos;
            ServerLevel level = sourceStack.getLevel();
            boolean alignX = (dx != 0);
            while (true) {
                BlockEntity be = level.getBlockEntity(current);
                if (be instanceof HopperBlockEntity hopper) {
                    // 读取第一个非空物品槽
                    ItemStack firstItem = ItemStack.EMPTY;
                    for (int i = 0; i < hopper.getContainerSize(); i++) {
                        ItemStack stack = hopper.getItem(i);
                        if (!stack.isEmpty()&& !stack.hasTag()) {
                            firstItem = stack;
                            break;
                        }
                    }

                    if (!firstItem.isEmpty()) {
                        String key = firstItem.getDescriptionId();
                        addItem(key,type,getAlignedPositions(current, startStorePos, endStorePos, alignX),getAlignedPositions(current, startChestPos, endChestPos, alignX));
                    }
                }

                if (current.equals(endPos)) break;
                current = current.offset(dx, 0, dz);
            }

            return 1;
        }
    }

    public static int deleteItemFromData(BlockPos startpos , BlockPos endpos, CommandSourceStack sourceStack){
        int dx = Integer.compare(endpos.getX(), startpos.getX());
        int dz = Integer.compare(endpos.getZ(), startpos.getZ());
        if ((dx != 0 && dz != 0) || (dx == 0 && dz == 0)) {
            sourceStack.sendFailure(Component.literal(tr("sducarpet.easycommand.allitemcommand1")));
            return 0;
        }
        BlockPos current = startpos;
        ServerLevel level = sourceStack.getLevel();
        while (true) {
            BlockEntity be = level.getBlockEntity(current);
            if (be instanceof HopperBlockEntity hopper) {
                ItemStack firstItem = ItemStack.EMPTY;
                for (int i = 0; i < hopper.getContainerSize(); i++) {
                    ItemStack stack = hopper.getItem(i);
                    if (!stack.isEmpty()&& !stack.hasTag()) {
                        firstItem = stack;
                        break;
                    }
                }
                if (!firstItem.isEmpty()) {
                    String key = firstItem.getDescriptionId();
                    delItem(key);
                }
            }
            if (current.equals(endpos)) break;
            current = current.offset(dx, 0, dz);
        }
        return 1;
    }

    private static HashSet<BlockPos> getAlignedPositions(BlockPos hopperPos, BlockPos startPos, BlockPos endPOS, boolean alignX) {
        HashSet<BlockPos> result = new HashSet<>();
        int minX = Math.min(startPos.getX(), endPOS.getX());
        int maxX = Math.max(startPos.getX(), endPOS.getX());
        int minY = Math.min(startPos.getY(), endPOS.getY());
        int maxY = Math.max(startPos.getY(), endPOS.getY());
        int minZ = Math.min(startPos.getZ(), endPOS.getZ());
        int maxZ = Math.max(startPos.getZ(), endPOS.getZ());

        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                for (int z = minZ; z <= maxZ; z++) {
                    if ((alignX && x == hopperPos.getX()) || (!alignX && z == hopperPos.getZ())) {
                        result.add(new BlockPos(x, y, z));
                    }
                }
            }
        }
        return result;
    }

}
