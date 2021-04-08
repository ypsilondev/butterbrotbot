package tech.ypsilon.bbbot.discord.command;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.events.message.priv.PrivateMessageReceivedEvent;
import tech.ypsilon.bbbot.util.StudentUtil;

/**
 * Command for handling email student verification
 *
 * @author Christian Schliz
 * @version 1.0
 */
public class VerifyCommand extends FullStackedExecutor {

    // The colors used here are default discord role
    // colors with their associated descriptions            // Discord Color - Color Name
    private static final int DISCORD_INFO = 0x5197D5;       // Medium Blue   - Blue Dart
    private static final int DISCORD_SUCCESS = 0x65C87A;    // Medium Green  - Snow Pea
    private static final int DISCORD_WARNING = 0xEAC545;    // Gold          - Liberator Gold
    private static final int DISCORD_ERROR = 0xD65745;      // Red           - Red Wire

    private static final MessageEmbed HELP_EMBED = new EmbedBuilder()
            .setColor(DISCORD_INFO)
            .setTitle("Help - Discord Verifizierung")
            .addField(
                    "Deutsch",
                    "Der KIT Discord Sever ist grundsätzlich für jedermann zugänglich. Wenn du am KIT studierst "
                            + "und Zugriff auf interne Kanäle (z.B. Informationen) haben möchtest, musst du dich "
                            + "vorher mit deiner studentischen E-Mail Adresse verifizieren. Sende mir daher **Per DM** "
                            + "dein u-Kürzel: `kit verify uxxxx`¹. Daraufhin bekommst du von dem Bot einen "
                            + "dreistelligen Code per Mail geschickt, den du danach mit `kit verify 000`² eingibst. "
                            + "Nach Eingabe des korrekten Codes kannst du auf alle interne Kanäle zugreifen. "
                            + "Falls du aktuell keinen Zugriff auf dein student.kit.edu E-Mail Postfach hast, kannst "
                            + "du alternativ einem der Moderatoren oder Admins per DM ein Bild deines "
                            + "Studentenausweises schicken. (Bild, Nachname und Matrikelnummer bitte zensieren)\n"
                            + "-----\n"
                            + "¹ ersetze uxxxx mit deinem persönlichen U-Kürzel"
                            + "² ersetze 000 mit dem per E-Mail gesendeten Code",
                    false
            )
            .addField(
                    "English",
                    "Generally speaking, the KIT Discord Server is accessible for everyone. If you are currently "
                            + "studying at KIT, consider verifying yourself in order to access certain private "
                            + "areas such as information channels. In order to begin the verification process, send "
                            + "me a private message containing `kit verify uxxxx`¹. You should recieve an email "
                            + "to your student.kit.edu account shortly containing a three digit code. Enter this code "
                            + "like this: `kit verify 000`². After entering the correct code, you may access all "
                            + "areas reserved for students and alumni. Alternatively if for example you currently "
                            + "have no access to your student.kit.edu mailbox you can send us a photo of your "
                            + "student id. Just make sure to censor the image, surname and matriculation number.\n"
                            + "-----\n"
                            + "¹ instead of uxxxx, enter your personal u-code"
                            + "² instead of 000, enter the verification code sent by mail",
                    false
            )
            .build();

    @Override
    public String[] getAlias() {
        return new String[]{"verify"};
    }

    @Override
    public String getDescription() {
        return "E-Mail Verifizierung um die Student-Rolle zu erhalten";
    }

    @Override
    public void onExecute(GuildMessageReceivedEvent event, String[] args) {
        event.getMessage().delete().queue();
        event.getAuthor().openPrivateChannel().flatMap(privateChannel ->
                privateChannel.sendMessage("For security reasons, the `verify` command "
                        + "is only allowed per direct message")).queue();
    }

    @Override
    public void onPrivateExecute(PrivateMessageReceivedEvent event, String[] args) {
        if (args.length < 1) {
            event.getChannel().sendMessage(HELP_EMBED).queue();
        } else if (StudentUtil.isStudentCode(args[0])) {
            // TODO: 1. check if user is already verified
            // TODO: 2. check if user has already initiated, but not completed a verification sequence
            // TODO: 3. check if u code is already verified under a different user
            // TODO: 4. if none of the above apply, initiate verification sequence
        } else {
            // TODO: 1. check if user is already verified
            // TODO: 2. check if user has already initiated, but not completed a verification sequence
                // TODO 2.1: check if code is correct and verify the user (+insert into database)
                // TODO 2.2: else send error
            // TODO: 3. check if u code is already verified under a different user
            // TODO: 4. if none of the above apply, send help message
        }
    }
}
