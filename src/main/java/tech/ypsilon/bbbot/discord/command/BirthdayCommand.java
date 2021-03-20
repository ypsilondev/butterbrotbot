package tech.ypsilon.bbbot.discord.command;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import org.bson.Document;
import tech.ypsilon.bbbot.database.MongoController;
import tech.ypsilon.bbbot.database.MongoSettings;
import tech.ypsilon.bbbot.database.codecs.BirthdayCodec;
import tech.ypsilon.bbbot.discord.ServiceManager;
import tech.ypsilon.bbbot.discord.services.BirthdayNotifierService;
import tech.ypsilon.bbbot.util.EmbedUtil;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Objects;

public class BirthdayCommand implements GuildExecuteHandler {

    private static final String DATE_REGEX = "\\d\\d?\\.\\d\\d?\\.\\d\\d\\d\\d";

    @Override
    public void onExecute(GuildMessageReceivedEvent e, String[] args) {
        Guild guild = e.getGuild();
        TextChannel channel = e.getChannel();

        if (args.length < 1) return;

        switch (args[0].toLowerCase()) {
            case "set":
                if (args[1].matches(DATE_REGEX)) {
                    // Own bday
                    saveBirthday(e.getMember().getIdLong(), parseDate(args[1], e), e.getMember().getUser());
                } else {
                    if (isBirthdayAdmin(e.getMember())) {
                        List<Member> mentioned = e.getMessage().getMentionedMembers();
                        if (mentioned.size() == 1) {
                            // Others bday
                            saveBirthday(mentioned.get(0).getIdLong(), parseDate(args[2], e), e.getMember().getUser());
                        }
                    }
                }
                break;

            case "remove":
                BirthdayCodec.newBirthday(e.getMember().getIdLong(), new Date(0));
                break;

            case "get":
                if (args.length > 1) {
                    List<Member> mentioned = e.getMessage().getMentionedMembers();
                    if (!mentioned.isEmpty()) {
                        for (Member m : mentioned) {
                            try{
                                Date bday = this.getBirthday(m.getIdLong());
                                String userAsMention = this.asMention(m.getIdLong(), guild);
                                if (!bday.equals(new Date(0))) {
                                    SimpleDateFormat formatter = new SimpleDateFormat("dd.MM.YYYY");
                                    e.getMember().getUser().openPrivateChannel().flatMap(privateChannel ->
                                            privateChannel.sendMessage(EmbedUtil.createSuccessEmbed()
                                                    .addField(userAsMention, formatter.format(bday), true).build())
                                    ).queue();
                                } else {
                                    e.getMember().getUser().openPrivateChannel().flatMap(privateChannel -> privateChannel.sendMessage(EmbedUtil.createErrorEmbed().addField(userAsMention, "Hat keinen Geburtstag angegeben", true).build())).queue();
                                }
                            } catch (NullPointerException e1){
                                e.getMember().getUser().openPrivateChannel().flatMap(privateChannel -> privateChannel.sendMessage(EmbedUtil.createErrorEmbed().addField("Datenbankabfrage", "Es gab einen Fehler, während das Geburtsdatum aus der Datenbank geladen wurde. Sollte dieses Problem weiterhin bestehen, wende dich bitte an einen Administrator.", false).build())).queue();
                            }
                        }
                    }
                }
                break;
            case "notify":
                if (this.isBirthdayAdmin(e.getMember())) {
                    ServiceManager.getInstance().findNotifierService(BirthdayNotifierService.class).execute(e.getChannel());
                }
                break;

            case "notifyhere":
                if (this.isBirthdayAdmin(e.getMember())) {
                    MongoSettings.setValue(MongoSettings.TYPE.BirthdayChannel, channel.getIdLong(), guild.getIdLong());

                    e.getAuthor().openPrivateChannel().flatMap(c -> c.sendMessage("Du hast erfolgreich den BDAY-Broadcast-Kanal festgelegt: " + channel.getAsMention())).queue();
                }
                break;

            case "time":
                Date date = new Date(System.currentTimeMillis());
                SimpleDateFormat formatter = new SimpleDateFormat("HH:mm:ss dd.MM.YYYY");
                String dateString = formatter.format(date);
                e.getMember().getUser().openPrivateChannel().flatMap(c -> c.sendMessage(EmbedUtil.createInfoEmbed().addField("TIME:", "Java sees following time: " + dateString, true).build())).queue();
                break;
            default:
                return;
        }
        e.getMessage().delete().queue();
    }

    private void saveBirthday(long userId, Date bday, User sender) {
        try {
            BirthdayCodec.newBirthday(userId, bday);
            sender.openPrivateChannel().flatMap(channel -> channel.sendMessage(EmbedUtil.createSuccessEmbed().addField("Geburtstag", "Der Geburtstag wurde erfolgreich gespeichert!", true).build())).queue();
        } catch (NullPointerException e1) {
            sender.openPrivateChannel().flatMap(channel -> channel.sendMessage(EmbedUtil.createErrorEmbed().addField("Datenbank", "Beim Hinzufügen des Geburtstags zur Datenbank ist leider ein Fehler aufgetreten. Bitte versuche es später erneut oder wende dich an einen Administrator.", false).build())).queue();
        }
    }

    private String asMention(long id, Guild g) {
        return g.getJDA().retrieveUserById(id).complete().getAsTag();
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

    private Date parseDate(String dateString, GuildMessageReceivedEvent e) {
        int day = 0;
        int month = 0;
        int year = 0;
        try {
            String[] dateComponents = dateString.split("\\.");
            day = Integer.parseInt(dateComponents[0]);
            month = Integer.parseInt(dateComponents[1]) - 1;
            year = Integer.parseInt(dateComponents[2]);
        } catch (NumberFormatException e1) {
            e.getMember().getUser().openPrivateChannel().flatMap(channel -> channel.sendMessage(EmbedUtil.createErrorEmbed().addField("Ungültiges Format", "Das eingegebene Datumsformat ist fehlerhaft!", false).build())).queue();
        }
        Calendar c = Calendar.getInstance();
        c.set(year, month, day, 0, 0, 0);
        return c.getTime();
    }

    @Override
    public String getDescription() {
        return "Der Geburtstagsbefehl: 'kit bday set <Geburtsdatum>'";
    }

    @Override
    public String[] getAlias() {
        return new String[]{"bday", "birthday"};
    }

    private boolean isBirthdayAdmin(Member member) {
        return Objects.requireNonNull(member).getRoles().stream().anyMatch(role -> role.getIdLong() == 759072770751201361L
                || role.getIdLong() == 757718320526000138L) || member.getUser().getIdLong() == 699011153208016926L;
    }
}
