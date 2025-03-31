package top.sducraft.helpers.rule.chunkLoadHelper;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.TicketType;
import net.minecraft.world.level.ChunkPos;
import java.util.Comparator;

public class RegistTicket {
    public static final TicketType<ChunkPos> FAKE_PEACE_TICKET_TYPE =TicketType.create("fakepeace", Comparator.comparingLong(ChunkPos::toLong),100);
    public static final TicketType<ChunkPos> ENDER_PEARL = TicketType.create("ender_pearl", Comparator.comparingLong(ChunkPos::toLong), 10);
    public static final TicketType<ChunkPos> End_GATEWAY_TICKET_TYPE = TicketType.create("end_gateway", Comparator.comparingLong(ChunkPos::toLong), 100);

    public static void addFakepeaceTicket(ServerLevel level, ChunkPos pos) {
        level.getChunkSource().addRegionTicket(FAKE_PEACE_TICKET_TYPE,pos,3,pos);
        level.resetEmptyTime();
    }

    public static void addEndGatewayTicket(ServerLevel level,ChunkPos pos) {
        level.getChunkSource().addRegionTicket(End_GATEWAY_TICKET_TYPE,pos,3,pos);
        level.resetEmptyTime();
    }

    public static void addEndPearlTicket(ServerLevel level, ChunkPos pos) {
        level.getChunkSource().addRegionTicket(ENDER_PEARL,pos,3,pos);
        level.resetEmptyTime();
    }
}
