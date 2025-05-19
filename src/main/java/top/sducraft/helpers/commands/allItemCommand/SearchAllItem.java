package top.sducraft.helpers.commands.allItemCommand;

import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.Style;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Display;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.phys.Vec3;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import static carpet.utils.Translations.tr;
import static top.sducraft.config.allItemData.AllItemData.*;
import static top.sducraft.helpers.commands.allItemCommand.ItemInfo.countItemInWorld;
import static top.sducraft.helpers.commands.allItemCommand.ItemInfo.getCountString;
import static top.sducraft.util.DelayedEventScheduler.addScheduleEvent;

public class SearchAllItem {
    public static int searchAndDisplay(String keyword, CommandSourceStack source) {
        itemData data = search(keyword);
        ServerPlayer player = source.getPlayer();
        if(player != null) {
            if (data != null && !data.storePos.isEmpty()) {
                for(BlockPos pos : data.chestPos) {
                    ServerLevel level = player.serverLevel();
                    Display.BlockDisplay entity = new Display.BlockDisplay(EntityType.BLOCK_DISPLAY, level);
                    entity.setBlockState(source.getLevel().getBlockState(pos));
                    entity.setGlowingTag(true);
                    entity.setPos(new Vec3(pos.getX(), pos.getY(), pos.getZ()));
                    entity.addTag("allitem_display");
                    level.addFreshEntity(entity);
                    addScheduleEvent(200, entity::discard);
                    player.lookAt(source.getAnchor(), entity.position().add(0.5, -1.5, 0.5));
                }
//                spawnItemDisplay(player.serverLevel(), data.chestPos, getItemByDescriptionId(descriptionId), 0xFFFF00);
                source.sendSuccess(() -> Component.literal(tr("成功搜索到物品") + keyword)
                        .withStyle(Style.EMPTY
                                .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND,"/allitem info "+keyword))
                                .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Component.literal(tr("点击查看 ")+keyword+tr("物品详细信息")))))
                        .append(Component.literal(tr(",当前储量为")+getCountString(countItemInWorld(data)))),false);
//                displayItemInfo(keyword, data, player);
                return 1;
            } else {
                List<String> fuzzyMatches = fuzzySearch(keyword.toLowerCase());
                if (fuzzyMatches.isEmpty()) {
                    source.sendFailure(Component.literal(tr("sducarpet.easycommand.allitemcommand2")).append(Component.literal("\""+keyword+"\"")));
                } else {
                    Component component = Component.empty();
                    for (String name : fuzzyMatches) {
                        component = Component.empty().append(component).append("\n").append(Component.literal(name)).withStyle(Style.EMPTY.withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/allitem search " + name)));
                    }
                    source.sendFailure(Component.literal(tr("sducarpet.easycommand.allitemcommand2")).append(Component.literal("\""+keyword+"\"")));
                    ;
                    player.displayClientMessage(Component.literal(tr("sducarpet.easycommand.allitemcommand3")).append(component), false);
                }
                return 0;
            }
        }
        return 0;
    }
    public static void deleteAllItemDisplay(MinecraftServer server) {
        for (ServerLevel level : server.getAllLevels()){
            for (Entity entity : level.getEntities().getAll()) {
                if(entity != null && entity.getTags().contains("allitem_display")) {
                    entity.discard();
                }
            }
        }
    }

    public static CompletableFuture<Suggestions> suggestFuzzyItemNames(SuggestionsBuilder builder) {
        String input = builder.getRemaining().toLowerCase(Locale.ROOT);
        List<String> smartSuggestions = new ArrayList<>();
        if (!input.isEmpty()) {
            smartSuggestions = fuzzySearch(input);
        }
        smartSuggestions.forEach(builder::suggest);
        return builder.buildFuture();
    }

}
