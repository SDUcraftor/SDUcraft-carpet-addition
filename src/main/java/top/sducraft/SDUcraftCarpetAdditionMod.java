package top.sducraft;

import carpet.CarpetExtension;
import carpet.CarpetServer;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.entity.FakePlayer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import top.sducraft.commands.CommandRegister;
import top.sducraft.config.LoadConfig;
import top.sducraft.easyCommand.WarningEasyCommand;
import top.sducraft.helpers.rule.fakePeaceHelper.FakePeaceHelper;
import top.sducraft.helpers.visualizers.Visualizers;
import top.sducraft.util.DelayedEventScheduler;

import java.util.Map;
import static carpet.utils.Translations.getTranslationFromResourcePath;
import static top.sducraft.helpers.commands.allItemCommand.SearchAllItem.deleteAllItemDisplay;
import static top.sducraft.helpers.rule.joinMessage.JoinMessage.showJoinMessage;
import static top.sducraft.util.DelayedEventScheduler.addScheduleEvent;


public class SDUcraftCarpetAdditionMod implements CarpetExtension, ModInitializer {
    public static String MOD_ID = "SDU-carpet";
    public final static Logger LOGGER = LogManager.getLogger(MOD_ID);

    static {
        CarpetServer.manageExtension(new SDUcraftCarpetAdditionMod());
    }

    @Override
    public void onInitialize() {
        ServerLifecycleEvents.SERVER_STARTED.register(LoadConfig::load);
        ServerLifecycleEvents.SERVER_STARTED.register(FakePeaceHelper::loadChunkOnInitialize);
        ServerLifecycleEvents.SERVER_STARTED.register(Visualizers::clearVisualizersOnServerStart);
        ServerTickEvents.START_SERVER_TICK.register(FakePeaceHelper::onServerTick);
        ServerTickEvents.START_SERVER_TICK.register(WarningEasyCommand::warnPlayer);
        ServerTickEvents.START_SERVER_TICK.register(DelayedEventScheduler::tick);
        addScheduleEvent(10,() -> { deleteAllItemDisplay(CarpetServer.minecraft_server);});
        CommandRegister.registerCommands();
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            if(!(handler.getPlayer() instanceof FakePlayer)) {showJoinMessage(handler.getPlayer());}
        });
    }

    @Override
    public void onGameStarted() {
        CarpetServer.settingsManager.parseSettingsClass(SDUcraftCarpetSettings.class);
        LOGGER.info("[SDU] Mod Loaded.");
    }

    @Override
    public Map<String, String> canHasTranslations(String lang) {
        Map<String, String> langdict = getTranslationFromResourcePath(String.format("assets/sdu/lang/%s.json",lang));
        if (langdict == null)
            langdict = getTranslationFromResourcePath("assets/sdu/lang/en_us.json");
        return langdict;
    }
}
