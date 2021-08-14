package tech.ypsilon.bbbot.discord.command.text;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.events.message.priv.PrivateMessageReceivedEvent;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.slf4j.LoggerFactory;
import tech.ypsilon.bbbot.database.codecs.VerificationCodec;
import tech.ypsilon.bbbot.database.structs.VerificationDocument;
import tech.ypsilon.bbbot.discord.DiscordController;
import tech.ypsilon.bbbot.settings.SettingsController;
import tech.ypsilon.bbbot.util.DiscordUtil;
import tech.ypsilon.bbbot.util.EmbedUtil;
import tech.ypsilon.bbbot.util.StudentUtil;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.Properties;

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
            .setDescription("\\*\\*ENGLISH VERSION BELOW\\*\\*")
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
                            + "¹ ersetze uxxxx mit deinem persönlichen U-Kürzel\n"
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
                            + "¹ instead of uxxxx, enter your personal u-code\n"
                            + "² instead of 000, enter the verification code sent by mail",
                    false
            )
            .build();

    private Properties mailSessionProperties;

    public VerifyCommand() {
        mailSessionProperties = new Properties();
        mailSessionProperties.put("mail.smtp.host", SettingsController.getValue("mail.smtp.host"));
        mailSessionProperties.put("mail.smtp.port", SettingsController.getValue("mail.smtp.port").toString());
        mailSessionProperties.put("mail.smtp.auth", "true");
        mailSessionProperties.put("mail.smtp.socketFactory.port", SettingsController.getValue("mail.smtp.port").toString());
        mailSessionProperties.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
    }

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
        if(DiscordUtil.isAdmin(event.getMember())) {
            for (Member mentionedMember : event.getMessage().getMentionedMembers()) {
                VerificationDocument document = new VerificationDocument(new ObjectId(), event.getAuthor().getIdLong());
                document.setVerified(true);
                VerificationCodec.save(document);

                Role student = event.getJDA()
                        .getRoleById((long) SettingsController.getValue("discord.roles.student"));
                assert student != null;
                DiscordController.getHomeGuild().addRoleToMember(mentionedMember.getIdLong(), student).queue();

                event.getChannel().sendMessage(EmbedUtil
                        .colorDescriptionBuild(DISCORD_SUCCESS, "Verified user "
                                + mentionedMember.getAsMention())).queue();
            }
        } else {
            event.getMessage().delete().queue();
            event.getAuthor().openPrivateChannel().flatMap(privateChannel ->
                    privateChannel.sendMessage("For security reasons, the `verify` command "
                            + "is only allowed per direct message")).queue();
        }
    }

    @Override
    public void onPrivateExecute(PrivateMessageReceivedEvent event, String[] args) {
        if (isVerified(event.getAuthor().getIdLong())) {
            // user already verified
            event.getChannel().sendMessage(EmbedUtil.colorDescriptionBuild(DISCORD_WARNING,
                    "Du bist schon verifiziert / Already verified")).queue();
        } else if (args.length < 1) {
            // too few arguments specified
            event.getChannel().sendMessage(HELP_EMBED).queue();
        } else if (StudentUtil.isStudentCode(args[0])) {
            // user sent student u-code
            if (isVerifying(event.getAuthor().getIdLong())) {
                // check if user has already initiated, but not completed a verification sequence
                event.getChannel().sendMessage(EmbedUtil.colorDescriptionBuild(DISCORD_ERROR,
                        "Du hast bereits eine Anfrage geschickt / "
                                + "You've already submitted a verification request")).queue();
            } else if (studentCodeTaken(args[0])) {
                // check if u code is already verified under a different user
                event.getChannel().sendMessage(EmbedUtil.colorDescriptionBuild(DISCORD_ERROR,
                        "Dieses u Kürzel wurde bereits verwendet, um einen anderen User zu verifizieren / "
                                + "This student code is already in use by another account")).queue();
            } else {
                // if none of the above apply, initiate verification sequence
                VerificationCodec.insert(event.getAuthor().getIdLong(), args[0]);
                try {
                    sendEmail(event.getAuthor().getIdLong(), args[0]);
                    event.getChannel().sendMessage(EmbedUtil.createDefaultEmbed()
                            .setColor(DISCORD_WARNING)
                            .addField("Deutsch", "Eine Bestätigungsmail wurde an `" + args[0]
                                    + "@student.kit.edu` gesendet. Bestätige diesen Account nun mit dem Befehl "
                                    + "`kit verify <Bestätigungscode>`.", false)
                            .addField("English", "A verification email was sent to `"
                                    + args[0] + "@student.kit.edu`. Verify your account with the command "
                                    + "`kit verify <verification_code>`.", false)
                            .build()).queue();
                } catch (MessagingException exception) {
                    event.getChannel().sendMessage(EmbedUtil.colorDescriptionBuild(DISCORD_ERROR,
                            "Die E-Mail konnte nicht gesendet werden, bitte wende dich an ein Teammitglied / "
                                    + "The email couldn't be sent, please contact the server staff")).queue();
                    exception.printStackTrace();
                    LoggerFactory.getLogger(VerifyCommand.class).error("");
                }
            }
        } else {
            // user send non-student code (could be a verification code)
            if (isVerifying(event.getAuthor().getIdLong())) {
                // check if user has already initiated, but not completed a verification sequence
                VerificationDocument verificationDocument = VerificationCodec.getCollection()
                        .find(new Document(VerificationCodec.FIELD_USER_ID, event.getAuthor().getIdLong())).first();

                assert verificationDocument != null;
                if (args[0].equalsIgnoreCase(verificationDocument.getVerificationCode())) {
                    // check if code is correct and verify the user (+insert into database)
                    Role student = event.getJDA()
                            .getRoleById((long) SettingsController.getValue("discord.roles.student"));

                    assert student != null;
                    DiscordController.getHomeGuild().addRoleToMember(event.getAuthor().getIdLong(), student).queue();

                    verificationDocument.setVerified(true);
                    VerificationCodec.save(verificationDocument);

                    event.getChannel().sendMessage(EmbedUtil.colorDescriptionBuild(DISCORD_SUCCESS,
                            "Du hast dich erfolgreich verifiziert / "
                                    + "You have been verified successfully")).queue();
                } else {
                    // else send error on incorrect code
                    event.getChannel().sendMessage(EmbedUtil.colorDescriptionBuild(DISCORD_ERROR,
                            "Falscher Bestätigungscode / "
                                    + "Incorrect verification code")).queue();
                }
            } else {
                // verification was not already initiated and input wasn't a student code
                event.getChannel().sendMessage(HELP_EMBED).queue();
            }
        }
    }

    private boolean isVerified(long userId) {
        VerificationDocument verificationDocument = VerificationCodec.getCollection()
                .find(new Document(VerificationCodec.FIELD_USER_ID, userId)).first();
        return verificationDocument != null && verificationDocument.getVerified();
    }

    private boolean isVerifying(long userId) {
        VerificationDocument verificationDocument = VerificationCodec.getCollection()
                .find(new Document(VerificationCodec.FIELD_USER_ID, userId)).first();
        return verificationDocument != null && !verificationDocument.getVerified();
    }

    private boolean studentCodeTaken(String studentCode) {
        VerificationDocument verificationDocument = VerificationCodec.getCollection()
                .find(new Document(VerificationCodec.FIELD_STUDENT_CODE, studentCode)).first();
        return verificationDocument != null && verificationDocument.getVerified();
    }

    private void sendEmail(long userId, String recipient) throws MessagingException {
        Session session = Session.getInstance(mailSessionProperties, new javax.mail.Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(
                        (String) SettingsController.getValue("mail.smtp.address"),
                        (String) SettingsController.getValue("mail.smtp.password")
                );
            }
        });

        VerificationDocument document = VerificationCodec.insert(userId, recipient);

        Message message = new MimeMessage(session);
        message.setFrom(new InternetAddress((String) SettingsController.getValue("mail.smtp.address")));
        message.setRecipients(
                Message.RecipientType.TO,
                InternetAddress.parse(recipient + "@student.kit.edu")
        );

        message.setSubject("Verification Code: " + document.getVerificationCode());
        message.setText("Hi,\n\nYour Verification Code is: " + document.getVerificationCode()
                + "\n\nWe wish you a great time on the Discord KIT Guild!");

        Transport.send(message);
    }
}
