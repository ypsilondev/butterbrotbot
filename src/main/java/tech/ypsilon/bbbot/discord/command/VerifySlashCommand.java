package tech.ypsilon.bbbot.discord.command;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.slf4j.LoggerFactory;
import tech.ypsilon.bbbot.database.codecs.VerificationCodec;
import tech.ypsilon.bbbot.database.structs.VerificationDocument;
import tech.ypsilon.bbbot.discord.DiscordController;
import tech.ypsilon.bbbot.discord.command.text.VerifyCommand;
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
import java.util.Calendar;
import java.util.Objects;
import java.util.Properties;

public class VerifySlashCommand extends SlashCommand {
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
                            + "dein u-Kürzel: `/verify email:uxxxx`¹. Daraufhin bekommst du von dem Bot einen "
                            + "dreistelligen Code per Mail geschickt, den du danach mit `/verify code:000`² eingibst. "
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
                            + "me a private message containing `/verify email:uxxxx`¹. You should recieve an email "
                            + "to your student.kit.edu account shortly containing a three digit code. Enter this code "
                            + "like this: `/verify code:000`². After entering the correct code, you may access all "
                            + "areas reserved for students and alumni. Alternatively if for example you currently "
                            + "have no access to your student.kit.edu mailbox you can send us a photo of your "
                            + "student id. Just make sure to censor the image, surname and matriculation number.\n"
                            + "-----\n"
                            + "¹ instead of uxxxx, enter your personal u-code\n"
                            + "² instead of 000, enter the verification code sent by mail",
                    false
            )
            .build();

    private final Properties mailSessionProperties;

    public VerifySlashCommand() {
        mailSessionProperties = new Properties();
        mailSessionProperties.put("mail.smtp.host", SettingsController.getValue("mail.smtp.host"));
        mailSessionProperties.put("mail.smtp.port", SettingsController.getValue("mail.smtp.port").toString());
        mailSessionProperties.put("mail.smtp.auth", "true");
        mailSessionProperties.put("mail.smtp.socketFactory.port", SettingsController.getValue("mail.smtp.port").toString());
        mailSessionProperties.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
    }

    @Override
    public CommandData commandData() {
        return new CommandData("verify", "Verifiziert dich als KIT-Student").addOptions(
                new OptionData(OptionType.STRING, "ukuerzel", "Dein u-Kürzel"),
                new OptionData(OptionType.STRING, "code", "Dein Verifizierungscode"),
                new OptionData(OptionType.USER, "user", "[ADMIN ONLY] Der zu verifizierende Benutzer")
        );
    }

    @Override
    public void execute(SlashCommandEvent event) {
        event.deferReply(true).queue();

        OptionMapping memberMapping = event.getOption("user");
        OptionMapping studentCodeMapping = event.getOption("ukuerzel");
        OptionMapping codeMapping = event.getOption("code");

        if (DiscordUtil.isAdmin(event.getMember()) && memberMapping != null) {
            Member mentionedMember = memberMapping.getAsMember();
            if (mentionedMember == null) {
                throw new CommandFailedException("Member can't be null!");
            }
            VerificationDocument document = VerificationCodec.getCollection().find(new Document("userId", mentionedMember.getIdLong())).first();
            if(document == null) {
                if(studentCodeMapping == null) {
                    event.getHook().editOriginalEmbeds(EmbedUtil.createErrorEmbed().setTitle("Fail").setDescription("Dieser Benutzer hat noch keinen Verifizierungsantrag gestellt. Er*sie muss erst `/verify uKuerzel` eigeben!").build()).queue();
                    return;
                }
                document = new VerificationDocument(new ObjectId(), mentionedMember.getIdLong(), studentCodeMapping.getAsString(), "000", true, Calendar.getInstance().getTime());
            }
            document.setVerified(true);

            VerificationCodec.save(document);

            Role student = event.getJDA()
                    .getRoleById((long) SettingsController.getValue("discord.roles.student"));
            assert student != null;
            DiscordController.getHomeGuild().addRoleToMember(mentionedMember.getIdLong(), student).queue();

            event.getHook().editOriginalEmbeds(EmbedUtil
                    .colorDescriptionBuild(DISCORD_SUCCESS, "Verified user "
                            + mentionedMember.getAsMention())).queue();
            return;
        }
        if (isVerified(event.getUser().getIdLong())) {
            // user already verified
            event.getHook().editOriginalEmbeds(EmbedUtil.colorDescriptionBuild(DISCORD_WARNING,
                    "Du bist schon verifiziert / Already verified")).queue();
            return;
        }

        if (studentCodeMapping != null) {
            String studentCode = studentCodeMapping.getAsString();
            if (StudentUtil.isStudentCode(studentCode)) {
                // user sent student u-code
                if (isVerifying(event.getUser().getIdLong())) {
                    // check if user has already initiated, but not completed a verification sequence
                    event.getHook().editOriginalEmbeds(EmbedUtil.colorDescriptionBuild(DISCORD_ERROR,
                            "Du hast bereits eine Anfrage geschickt / "
                                    + "You've already submitted a verification request")).queue();
                } else if (studentCodeTaken(studentCode)) {
                    // check if u code is already verified under a different user
                    event.getHook().editOriginalEmbeds(EmbedUtil.colorDescriptionBuild(DISCORD_ERROR,
                            "Dieses u Kürzel wurde bereits verwendet, um einen anderen User zu verifizieren / "
                                    + "This student code is already in use by another account")).queue();
                } else {
                    // if none of the above apply, initiate verification sequence
                    // VerificationCodec.insert(event.getUser().getIdLong(), studentCode);
                    try {
                        sendEmail(event.getUser().getIdLong(), studentCode);
                        event.getHook().editOriginalEmbeds(EmbedUtil.createDefaultEmbed()
                                .setColor(DISCORD_WARNING)
                                .addField("Deutsch", "Eine Bestätigungsmail wurde an `" + studentCode
                                        + "@student.kit.edu` gesendet. Bestätige diesen Account nun mit dem Befehl "
                                        + "`/verify code:<Bestätigungscode>`.", false)
                                .addField("English", "A verification email was sent to `"
                                        + studentCode + "@student.kit.edu`. Verify your account with the command "
                                        + "`/verify code:<verification_code>`.", false)
                                .build()).queue();
                    } catch (MessagingException exception) {
                        event.getHook().editOriginalEmbeds(EmbedUtil.colorDescriptionBuild(DISCORD_ERROR,
                                "Die E-Mail konnte nicht gesendet werden, bitte wende dich an ein Teammitglied / "
                                        + "The email couldn't be sent, please contact the server staff")).queue();
                        exception.printStackTrace();
                        LoggerFactory.getLogger(VerifyCommand.class).error("");
                    }
                }
            } else {
                event.getHook().editOriginalEmbeds(EmbedUtil.createErrorEmbed().setTitle("Fail").setDescription("The provided string doesn't seem to be a kit-student-identifier :c").build()).queue();
            }
            return;
        }

        if (codeMapping != null) {
            // user send non-student code (could be a verification code)
            if (isVerifying(event.getUser().getIdLong())) {
                // check if user has already initiated, but not completed a verification sequence
                VerificationDocument verificationDocument = VerificationCodec.getCollection()
                        .find(new Document(VerificationCodec.FIELD_USER_ID, event.getUser().getIdLong())).first();

                assert verificationDocument != null;
                if (codeMapping.getAsString().equalsIgnoreCase(verificationDocument.getVerificationCode())) {
                    // check if code is correct and verify the user (+insert into database)
                    Role student = event.getJDA()
                            .getRoleById((long) SettingsController.getValue("discord.roles.student"));

                    assert student != null;
                    DiscordController.getHomeGuild().addRoleToMember(event.getUser().getIdLong(), student).queue();

                    verificationDocument.setVerified(true);
                    VerificationCodec.save(verificationDocument);

                    event.getHook().editOriginalEmbeds(EmbedUtil.colorDescriptionBuild(DISCORD_SUCCESS,
                            "Du hast dich erfolgreich verifiziert / "
                                    + "You have been verified successfully")).queue();
                } else {
                    // else send error on incorrect code
                    event.getHook().editOriginalEmbeds(EmbedUtil.colorDescriptionBuild(DISCORD_ERROR,
                            "Falscher Bestätigungscode / "
                                    + "Incorrect verification code")).queue();
                }
            }
            return;
        }
        event.getHook().editOriginalEmbeds(HELP_EMBED).queue();
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
