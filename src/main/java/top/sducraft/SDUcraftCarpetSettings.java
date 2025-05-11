package top.sducraft;

import carpet.api.settings.CarpetRule;
import carpet.api.settings.Rule;
import carpet.api.settings.Validator;
import carpet.api.settings.Validators;
import carpet.utils.CommandHelper;
import net.minecraft.commands.CommandSourceStack;
import org.jetbrains.annotations.Nullable;

import static carpet.api.settings.RuleCategory.FEATURE;
import static top.sducraft.helpers.visualizers.Visualizers.updateVisualizers;

public class SDUcraftCarpetSettings {
    public static final String sdu= "SDU";
    public static final String Visualize = "Visualize";

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
    public static int itemPickUpDelay = 40;

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

    @Rule(
            validators = Validators.NonNegativeNumber.class,
            options = {"0","0.05","1"},
            strict = false,
            categories = {sdu}
    )
    public static double babyZombieOdds = 0.05;

    @Rule(
            validators = Validators.NonNegativeNumber.class,
            options = {"0","0.05","1"},
            strict = false,
            categories = {sdu}
    )
    public static double chickenJockeyGenerationOdds = 0.05;

    @Rule(
            validators = Validators.NonNegativeNumber.class,
            options = {"0","100","200","400"},
            strict = false,
            categories = {sdu}
    )
    public static int projectileRaycastLength = 0;


    @Rule(
            categories = {sdu}
    )
    public static boolean pearlTicketOptimization = false;

    @Rule(
            validators = UpdateVisualizer.class,
            categories = {Visualize, FEATURE}
    )
    public static boolean hopperCooldownVisualize = false;

//    @Rule(
//            validators = UpdateVisualizer.class,
//            categories = {Visualize, FEATURE}
//    )
//    public static boolean BlockEventVisualize = false;

    private static class NotifyPlayers<T> extends Validator<T> {
        @Override
        public T validate(@Nullable CommandSourceStack commandSourceStack, CarpetRule<T> carpetRule, T t, String s) {
            if (commandSourceStack != null) {
                CommandHelper.notifyPlayersCommandsChanged(commandSourceStack.getServer());
            }
            return t;
        }
    }

    private static class UpdateVisualizer<T> extends Validator<T> {
        @Override
        public T validate(@Nullable CommandSourceStack commandSourceStack, CarpetRule<T> carpetRule, T t, String s) {
            if (commandSourceStack!= null) {
                updateVisualizers(commandSourceStack.getServer());
            }
            return t;
        }
    }

}
