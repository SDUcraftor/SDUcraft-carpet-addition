package top.sducraft.helpers.rule.fakePeaceHelper;

import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import top.sducraft.SDUcraftCarpetSettings;
import top.sducraft.config.rule.easyfakePeaceConfig;
import static top.sducraft.helpers.rule.chunkLoadHelper.RegistTicket.addFakepeaceTicket;

public class fakePeaceHelper {
    private static int tickCounter = 0;

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

    private static void loadchunk(MinecraftServer server, ServerLevel dimension){
        String dimensionKey = dimension.dimension().toString();
        ServerLevel targetDimension = switch (dimensionKey) {
            case "ResourceKey[minecraft:dimension / minecraft:overworld]" -> server.getLevel(Level.NETHER);
            case "ResourceKey[minecraft:dimension / minecraft:the_nether]" -> server.getLevel(Level.OVERWORLD);
            case "ResourceKey[minecraft:dimension / minecraft:the_end]" -> server.getLevel(Level.END);
            default -> null;
        };

        if (targetDimension != null) {
            BlockPos pos = easyfakePeaceConfig.getFakePeaceCoordinates(dimensionKey);
            if(pos != null) {
                addFakepeaceTicket(targetDimension,new ChunkPos(pos));
            }
        }
    }

    public static void onServerTick(MinecraftServer server) {
        tickCounter++;
        if (tickCounter >= 1000) {
            tickCounter = 0;
            loadChunkOnInitialize(server);
        }
    }
}

