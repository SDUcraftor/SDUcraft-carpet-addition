package top.sducraft;

import carpet.api.settings.CarpetRule;
import carpet.api.settings.Rule;
import carpet.api.settings.Validator;
import carpet.api.settings.Validators;
import carpet.utils.CommandHelper;
import net.minecraft.commands.CommandSourceStack;

public class SDUcraftCarpetSettings {
    public static final String sdu= "SDU";

    @Rule(categories = {sdu})
    public static boolean armorStandIgnoreShulkerDamage = false;

    @Rule(categories = {sdu})
    public static boolean brittleDeepSlate = false;

    @Rule(  categories = {sdu},
            options = {"true", "false"},
            validators = NotifyPlayers.class)
    public static boolean tickRateChangedMessage = false;

    @Rule(  categories = {sdu},
            options = {"true", "false"},
            validators = NotifyPlayers.class)
    public static boolean easyFakePeace = false;

    @Rule(  categories = {sdu},
            options = {"true", "false"},
            validators = NotifyPlayers.class)
    public static boolean easyCommand = false;

    @Rule(
            validators = Validators.NonNegativeNumber.class,
            options = {"0", "40", "80", "32767"},
            strict = false,
            categories = {sdu}
    )
    public static int itempickupDelay = 40;

    @Rule(categories = {sdu})
    public static boolean tntTeleportThroughNetherPortal =false;

    @Rule(categories = {sdu})
    public static boolean disableNetherPortal = false;

    @Rule(
            validators = Validators.NonNegativeNumber.class,
            options = {"0", "300", "1000"},
            strict = false,
            categories = {sdu}
    )
    public static int netherPortalCooldown = 300;

    @Rule(
            validators = Validators.NonNegativeNumber.class,
            options = {"2"},
            strict = false,
            categories = {"firework"}
    )
    public static int fireworkParticleNumber = 2;

    @Rule(
            validators = Validators.NonNegativeNumber.class,
            options = {"0.25"},
            strict = false,
            categories = {"firework"}
    )
    public static double fireworkRange = 0.25;

    @Rule(
            validators = Validators.NonNegativeNumber.class,
            options = {"4"},
            strict = false,
            categories = {"firework"}
    )
    public static int bigfireworkParticleNumber = 4;

    @Rule(
            validators = Validators.NonNegativeNumber.class,
            options = {"0.5"},
            strict = false,
            categories = {"firework"}
    )
    public static double bigfireworkRange = 0.5;

    @Rule(categories = {sdu})
    public static boolean endGatewayTicket = false;

    private static class NotifyPlayers extends Validator<Boolean> {
        @Override
        public Boolean validate(CommandSourceStack source, CarpetRule<Boolean> changingRule, Boolean newValue, String userInput) {
            if (source != null) {
                CommandHelper.notifyPlayersCommandsChanged(source.getServer());
            }
            return newValue;
        }
    }
}
