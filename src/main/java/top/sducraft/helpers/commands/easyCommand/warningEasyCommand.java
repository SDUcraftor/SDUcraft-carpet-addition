package top.sducraft.helpers.commands.easyCommand;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.protocol.game.ClientboundSetTitleTextPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;
import top.sducraft.SDUcraftCarpetSettings;
import top.sducraft.config.rule.warningConfig;
import static carpet.utils.Translations.tr;
import static top.sducraft.config.rule.warningConfig.warningList;
import static top.sducraft.util.massageComponentCreate.createCommandClickComponent;
import static top.sducraft.util.massageComponentCreate.getDimensionColor;

public class warningEasyCommand implements IEasyCommand{
    private static int tickcount = 0 ;

    @Override
    public String getCommandName() {
        return "warning";
    }

    @Override
    public Component clickButton() {
        return createCommandClickComponent("[警告系统]", "/easycommand warning","点击进入警告配置界面");
    }

    @Override
    public void showEasyCommandInterface(ServerPlayer player) {
        Component component = Component.literal("\n[warning指令介绍]\n").withStyle(Style.EMPTY.withColor(ChatFormatting.GRAY))
//                        .withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://mcdreforged.com/zh-CN/plugin/gamemode"))
//                        .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Component.literal("点击查看!!spec命令使用文档"))))
                .append(Component.literal(tr("sducarpet.easycommand.warningcommand8")))
                .append(Component.literal(tr("sducarpet.easycommand.warningcommand9")).withStyle(Style.EMPTY.withColor(ChatFormatting.WHITE)));
        player.displayClientMessage(component, false);
        showWarningList(player);
    }

    public static void showWarningList(ServerPlayer player) {
        if (warningList.isEmpty()) {
            player.displayClientMessage(Component.literal(tr("sducarpet.easycommand.warningcommand7")), false);
            return;
        }
        Component header = Component.literal(tr("sducarpet.easycommand.warningcommand10"));
        Component body = Component.empty();
        for (warningConfig.warning warning : warningList) {
            Component info = Component.literal("\n" + warning.name + "  ")
                    .append(Component.literal("(" + warning.pos.toShortString() + ") "))
                    .append(Component.literal("@" + warning.dimension)
                            .withColor(getDimensionColor(warning.dimension)))
                    .append(Component.literal(" \"" + warning.text + "\"  "));
            Component buttons = createWarningStateText(warning);
            body =Component.empty().append(body).append(info).append(buttons);
        }
        player.displayClientMessage(Component.empty().append(header).append(body), false);
    }

    private static Component createWarningStateText(warningConfig.warning warning) {
        boolean state = warning.status;
        String name = warning.name;
        Component trueButton;
        Component falseButton;
        if (state) {
            trueButton = Component.literal("[true] ")
                    .withStyle(Style.EMPTY
                            .withBold(true)
                            .withUnderlined(true)
                            .withColor(ChatFormatting.AQUA)
                    );

            falseButton = Component.literal("[false]")
                    .withStyle(Style.EMPTY
                            .withClickEvent(new ClickEvent(
                                    ClickEvent.Action.RUN_COMMAND,
                                    "/warning set \"" + name + "\" false"
                            ))
                            .withHoverEvent(new HoverEvent(
                                    HoverEvent.Action.SHOW_TEXT,
                                    Component.literal(tr("warningcommand12")).append(name)
                            ))
                            .withColor(ChatFormatting.GRAY)
                    );
        } else {
            trueButton = Component.literal("[true] ")
                    .withStyle(Style.EMPTY
                            .withClickEvent(new ClickEvent(
                                    ClickEvent.Action.RUN_COMMAND,
                                    "/warning set \"" + name + "\" true"
                            ))
                            .withHoverEvent(new HoverEvent(
                                    HoverEvent.Action.SHOW_TEXT,
                                    Component.literal("warningcommand13").append(name)
                            ))
                            .withColor(ChatFormatting.GRAY)
                    );

            falseButton = Component.literal("[false]")
                    .withStyle(Style.EMPTY
                            .withBold(true)
                            .withUnderlined(true)
                            .withColor(ChatFormatting.AQUA)
                    );
        }

        return Component.empty().append(trueButton).append(falseButton);
    }


    public static void warnPlayer(MinecraftServer server) {
        if (SDUcraftCarpetSettings.easyCommand) {
            tickcount++;
            if (tickcount >= 20) {
                tickcount = 0;
                PlayerList playerList = server.getPlayerList();
                for (ServerPlayer player : playerList.getPlayers()) {
                    for (warningConfig.warning warning : warningList) {
                        if (warning.status) {
                            if (player.serverLevel().dimension().location().getPath().equals(warning.dimension) && Math.abs(player.getX() - warning.pos.getX()) <= 250 && Math.abs(player.getZ() - warning.pos.getZ()) <= 250) {
                                  ClientboundSetTitleTextPacket titleTextPacket = new ClientboundSetTitleTextPacket(Component.literal(tr("sducarpet.easycommand.fakepeacewarn1")).withStyle(Style.EMPTY.withColor(ChatFormatting.RED)));
                                  player.connection.send(titleTextPacket);
                                  player.sendSystemMessage(Component.literal(warning.text+" ").append(Component.literal(tr("sducarpet.easycommand.warningcommand11")).withColor(0xFF5555)).append(Component.literal("("+warning.pos.toShortString()+")")).append(Component.literal(warning.dimension).withColor(getDimensionColor(warning.dimension))), true);
                                  drawDirectionArrow(player, warning.pos);
                            }
                        }
                    }
                }
            }
        }
    }

    private static void drawDirectionArrow(ServerPlayer player, BlockPos targetPos) {
        ServerLevel level = player.serverLevel();
        double dx = player.getX() - (targetPos.getX() + 0.5);
        double dz = player.getZ() - (targetPos.getZ() + 0.5);
        double dist = Math.hypot(dx, dz);
        double dirX = dx / dist;
        double dirZ = dz / dist;
        double startBack   = -41.0;
        double endFront    = 45.0;
        double step        = 15.0;
        double shaftLen    = 8.0;
        double headLen     = 3.0;
        double headAngle   = Math.PI / 4;
        ParticleOptions particle = new DustParticleOptions(
                new Vector3f(0f, 1f, 0f), 1.0f
        );
        for (double offset = startBack; offset <= endFront; offset += step) {
            Vec3 base = player.position().add(dirX * offset, 0, dirZ * offset);
            for (double t = 0; t <= shaftLen; t += 0.2) {
                Vec3 p = base.add(dirX * t, 0, dirZ * t);
                level.sendParticles(particle, p.x, p.y, p.z, 1, 0, 0, 0, 0);
            }
            Vec3 tip  = base.add(dirX * shaftLen, 0, dirZ * shaftLen);
            Vec3 back = new Vec3(-dirX, 0, -dirZ);
            double cosAng = Math.cos(headAngle);
            double sinAng = Math.sin(headAngle);
            Vec3 leftHeadDir = new Vec3(back.x * cosAng - back.z * sinAng, 0, back.x * sinAng + back.z * cosAng).normalize();
            Vec3 rightHeadDir = new Vec3(back.x * cosAng + back.z * sinAng, 0, -back.x * sinAng + back.z * cosAng).normalize();
            for (double t = 0; t <= headLen; t += 0.2) {
                Vec3 pL = tip.add(leftHeadDir.scale(t));
                Vec3 pR = tip.add(rightHeadDir.scale(t));
                level.sendParticles(particle, pL.x, pL.y, pL.z, 1, 0, 0, 0, 0);
                level.sendParticles(particle, pR.x, pR.y, pR.z, 1, 0, 0, 0, 0);
            }
        }
    }
}
