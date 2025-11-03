package top.sducraft.helpers.rule.chunkLoadHelper;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;

import static top.sducraft.SDUcraftCarpetAdditionMod.FAKE_PEACE_TICKET_TYPE;

public class RegistTicket {

    public static void addFakepeaceTicket(ServerLevel level, ChunkPos pos) {
        level.getChunkSource().addTicketWithRadius(FAKE_PEACE_TICKET_TYPE, pos, 3);
        level.resetEmptyTime();
    }

}
