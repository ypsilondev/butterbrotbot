package tech.ypsilon.bbbot.discord.command;

import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import tech.ypsilon.bbbot.util.EmbedUtil;

import java.text.SimpleDateFormat;
import java.util.Date;

public class BirthdayCommand extends SlashCommand {

    private static final String DATE_REGEX = "\\d\\d?\\.\\d\\d?\\.\\d\\d\\d\\d";

    @Override
    public CommandData commandData() {
        return new CommandData("birthday", "Der Geburtstagsbefehl").addSubcommands(
                new SubcommandData("set", "Setze deinen Geburtstag")
                        .addOption(OptionType.STRING, "date", "Geburtsdatum (DD.MM.YYYY)", true),
                new SubcommandData("get", "Erhalte den Geburtstag eines anderen Mitglieds")
                        .addOption(OptionType.USER, "member", "member", true)
        );
    }

    @Override
    public boolean isGlobal() {
        return true;
    }

    @Override
    public void execute(SlashCommandEvent event) {
        if (event.getSubcommandName() == null) throw new CommandFailedException("Es ist ein Fehler aufgetreten");

        if (event.getSubcommandName().equalsIgnoreCase("set")) {

        } else if (event.getSubcommandName().equalsIgnoreCase("get")) {

        }
    }

    protected void get(SlashCommandEvent event) {
        /**
        if (event.getOption("member") == null) throw new CommandFailedException("Bitte versuche es erneut");

        Member target = event.getOption("member").getAsMember();

        if (target == null) throw new CommandFailedException("Dieser Benutzer konnte nicht gefunden werden!");
        try {
            Date bday = this.getBirthday(target.getIdLong());
            String userAsMention = target.getAsMention();
            if (!bday.equals(new Date(0))) {
                SimpleDateFormat formatter = new SimpleDateFormat("dd.MM.YYYY");
                e.getMember().getUser().openPrivateChannel().flatMap(privateChannel ->
                        privateChannel.sendMessage(EmbedUtil.createSuccessEmbed()
                                .addField(userAsMention, formatter.format(bday), true).build())
                ).queue();
            } else {
                e.getMember().getUser().openPrivateChannel().flatMap(privateChannel -> privateChannel.sendMessage(EmbedUtil.createErrorEmbed().addField(userAsMention, "Hat keinen Geburtstag angegeben", true).build())).queue();
            }
        } catch (NullPointerException e1) {
            e.getMember().getUser().openPrivateChannel().flatMap(privateChannel -> privateChannel.sendMessage(EmbedUtil.createErrorEmbed().addField("Datenbankabfrage", "Es gab einen Fehler, während das Geburtsdatum aus der Datenbank geladen wurde. Sollte dieses Problem weiterhin bestehen, wende dich bitte an einen Administrator.", false).build())).queue();
        }
         **/
    }
}
