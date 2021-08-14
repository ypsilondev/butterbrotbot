package tech.ypsilon.bbbot.discord.command;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import org.bson.Document;
import tech.ypsilon.bbbot.database.MongoController;
import tech.ypsilon.bbbot.database.codecs.BirthdayCodec;
import tech.ypsilon.bbbot.discord.services.BirthdayNotifierService;
import tech.ypsilon.bbbot.util.EmbedUtil;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Objects;

public class BirthdayCommand extends SlashCommand {

    private static final String DATE_REGEX = "\\d\\d?\\.\\d\\d?\\.\\d\\d\\d\\d";

    @Override
    public CommandData commandData() {
        return new CommandData("birthday", "Der Geburtstagsbefehl").addSubcommands(
                new SubcommandData("set", "Setze deinen Geburtstag")
                        .addOption(OptionType.STRING, "date", "Geburtsdatum (DD.MM.YYYY)", true),
                new SubcommandData("get", "Erhalte den Geburtstag eines anderen Mitglieds")
                        .addOption(OptionType.USER, "member", "member", true),
                new SubcommandData("remove", "Lösche deinen Geburtstag")
        );
    }

    @Override
    public boolean isGlobal() {
        return true;
    }

    @Override
    public void execute(SlashCommandEvent event) {
        if (event.getSubcommandName() == null) throw new CommandFailedException("Es ist ein Fehler aufgetreten");

        switch (event.getSubcommandName()) {
            case "set":
                set(event);
                return;
            case "get":
                get(event);
                return;
            case "remove":
                return;
            default:
                throw new CommandFailedException("Es ist ein Fehler aufgetreten");
        }
    }

    protected void set(SlashCommandEvent event) {
        if (event.getOption("date") == null) throw new CommandFailedException("Bitte versuche es erneut");
        String optionDate = Objects.requireNonNull(event.getOption("date")).getAsString();

        if (optionDate.matches(DATE_REGEX)) {
            try {
                Date birthday = parseDate(optionDate);
                BirthdayCodec.newBirthday(event.getUser().getIdLong(), birthday);
                event.reply("Der Geburtstag wurde erfolgreich gespeichert!").queue();
            } catch (NullPointerException e1) {
                throw new CommandFailedException("Beim Hinzufügen des Geburtstags zur Datenbank ist leider ein Fehler aufgetreten. Bitte versuche es später erneut oder wende dich an einen Administrator.");
            }
        } else {
            throw new CommandFailedException("Das angegebene Datum ist ungültig. Bitte gib es nach dem Format (DD.MM.YYYY) an");
        }
    }

    protected void get(SlashCommandEvent event) {
        if (event.getOption("member") == null) throw new CommandFailedException("Bitte versuche es erneut");

        Member target = Objects.requireNonNull(event.getOption("member")).getAsMember();

        if (target == null) throw new CommandFailedException("Dieser Benutzer konnte nicht gefunden werden!");

        try {
            Date birthdayDate = this.getBirthday(target.getIdLong());

            if (!birthdayDate.equals(new Date(0))) {
                SimpleDateFormat formatter = new SimpleDateFormat("dd.MM.YYYY");

                event.reply(new MessageBuilder().setEmbeds(
                        EmbedUtil.createSuccessEmbed()
                                .addField(target.getAsMention(), formatter.format(birthdayDate), true).build()
                ).build()).queue();
            } else {
                throw new CommandFailedException(target.getAsMention() + " hat keinen Geburtstag angegeben");
            }
        } catch (NullPointerException e1) {
            throw new CommandFailedException("Es gab einen Fehler, während das Geburtsdatum "
                    + "aus der Datenbank geladen wurde. Sollte dieses Problem weiterhin bestehen, "
                    + "wende dich bitte an einen Administrator.");
        }
    }

    private Date getBirthday(long memberId) {
        MongoCollection<Document> collection = MongoController.getInstance().getCollection("Birthdays");
        MongoCursor<Document> cursor = collection.find().cursor();
        Document usr;
        while (cursor.hasNext()) {
            usr = cursor.next();
            if ((long) usr.get("userId") == memberId) {
                return (Date) usr.get("birthday");
            }
        }
        return new Date(0);
    }

    private Date parseDate(String dateString) {
        int day = 0;
        int month = 0;
        int year = 0;
        try {
            String[] dateComponents = dateString.split("\\.");
            day = Integer.parseInt(dateComponents[0]);
            month = Integer.parseInt(dateComponents[1]) - 1;
            year = Integer.parseInt(dateComponents[2]);
        } catch (NumberFormatException e1) {
            throw new CommandFailedException("Das eingegebene Datumsformat ist fehlerhaft!");
        }
        Calendar c = Calendar.getInstance();
        c.set(year, month, day, 0, 0, 0);
        return c.getTime();
    }
}
