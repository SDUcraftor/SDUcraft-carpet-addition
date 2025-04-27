package top.sducraft.helpers.rule.fakePeaceHelper;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.protocol.game.ClientboundSetSubtitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitleTextPacket;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import top.sducraft.SDUcraftCarpetSettings;
import top.sducraft.config.rule.easyfakePeaceConfig;

import javax.swing.*;

import static top.sducraft.helpers.rule.chunkLoadHelper.RegistTicket.addFakepeaceTicket;
import static top.sducraft.util.massageComponentCreate.getDimensionColor;

public class fakePeaceHelper {
    private static int tickCounter = 0;
    private static int tickCounter1 = 0;

    public static void loadChunkOnInitialize(MinecraftServer server) {
        if(SDUcraftCarpetSettings.easyFakePeace) {
            ServerLevel overworld = server.getLevel(Level.OVERWORLD);
            ServerLevel nether = server.getLevel(Level.NETHER);
            ServerLevel end = server.getLevel(Level.END);
            if (overworld != null) {
                loadchunk(server, overworld);
            }
            if (nether != null) {
                loadchunk(server, nether);
            }
            if (end != null) {
                loadchunk(server, end);
            }
        }
    }

    private static ServerLevel getTargetDimension (MinecraftServer server,ServerLevel level) {
        if (level == server.getLevel(Level.OVERWORLD)) {
            return server.getLevel(Level.NETHER);
        }
        else if (level == server.getLevel(Level.NETHER)) {
            return server.getLevel(Level.OVERWORLD);
        }
        else if (level == server.getLevel(Level.END)) {
            return server.getLevel(Level.END);
        }
        else {
            return level;
        }
    }

    private static void loadchunk(MinecraftServer server, ServerLevel dimension){
        if (SDUcraftCarpetSettings.easyFakePeace) {
            String dimensionKey = dimension.dimension().toString();
            ServerLevel targetDimension = getTargetDimension(server, dimension);
            if (targetDimension != null) {
                BlockPos pos = easyfakePeaceConfig.getFakePeaceCoordinates(dimensionKey);
                if (pos != null) {
                    addFakepeaceTicket(targetDimension, new ChunkPos(pos));
                }
            }
        }
    }

    public static void warnPlayer (MinecraftServer server){
        if (SDUcraftCarpetSettings.easyFakePeace) {
            tickCounter1++;
            if (tickCounter1 >= 50) {
                tickCounter1 = 0;
                PlayerList playerList = server.getPlayerList();
                for (ServerPlayer player : playerList.getPlayers()) {
                    String dimensionKey = getTargetDimension(server, player.serverLevel()).dimension().toString();
                    BlockPos pos = easyfakePeaceConfig.getFakePeaceCoordinates(dimensionKey);
                    if (pos != null && (Math.abs(player.getX() - pos.getX()) <= 300 && Math.abs(player.getZ() - pos.getZ()) <= 300)) {
                        ClientboundSetTitleTextPacket titleTextPacket = new ClientboundSetTitleTextPacket(Component.translatable("sducarpet.easycommand.fakepeacewarn1").withStyle(Style.EMPTY.withColor(ChatFormatting.RED)));
                        player.connection.send(titleTextPacket);
                        player.sendSystemMessage(Component.translatable("sducarpet.easycommand.fakepeacewarn2")
                                .append(Component.literal("(" + pos.toShortString() + ")").withStyle(Style.EMPTY.withColor(ChatFormatting.GREEN)))
                                .append(Component.literal("@"+player.serverLevel().dimension().location().getPath()).withColor(getDimensionColor(player.serverLevel().dimension().location().getPath()))),true);
                    }
                }
            }
        }
    }

    public static void onServerTick(MinecraftServer server) {
        warnPlayer(server);
        tickCounter++;
        if (tickCounter >= 1000) {
            tickCounter = 0;
            loadChunkOnInitialize(server);
        }
    }
}

