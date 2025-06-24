package top.sducraft.helpers.rule.chunkLoadHelper;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.TicketType;
import net.minecraft.world.level.ChunkPos;
import java.util.Comparator;

public class RegistTicket {
    public static final TicketType FAKE_PEACE_TICKET_TYPE =TicketType.register("fakepeace", 100L, false, TicketType.TicketUse.LOADING_AND_SIMULATION);


    public static void addFakepeaceTicket(ServerLevel level, ChunkPos pos) {
        level.getChunkSource().addTicketWithRadius(FAKE_PEACE_TICKET_TYPE,pos,3);
        level.resetEmptyTime();
    }

}
