package tech.ypsilon.bbbot.discord.command;

import net.dv8tion.jda.api.entities.Category;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.VoiceChannel;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import tech.ypsilon.bbbot.util.EmbedUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;

public class CreateChannelCommand implements GuildExecuteHandler {

    public static final List<VoiceChannel> channels = new ArrayList<>();
    private final HashMap<String, Integer> games = new HashMap<>();

    public CreateChannelCommand() {
        games.put("Among us", 10);
        games.put("Schach", 2);
        games.put("Overwatch", 6);
        games.put("League of Legends", 5);
        games.put("Coop", 2);
        games.put("Rainbow Six", 5);
        games.put("R6", 5);
        games.put("Rainbow Six Siege", 5);
        games.put("CS:GO", 5);
        games.put("CSGO", 5);
        games.put("Rocket League", 3);
        games.put("Dead by Daylight", 5);
        games.put("Apex Legends", 3);
        games.put("The Division 2", 4);
        games.put("For Honor", 4);
        games.put("Ghost Recon", 4);
        games.put("Uso", 8);
        games.put("Stronghold", 4);
        games.put("Destiny 2", 4);
        games.put("Valorant", 5);
    }

    @Override
    public String[] getAlias() {
        return new String[]{"createchannel", "cc", "customchannel"};
    }

    @Override
    public String getDescription() {
        return "Erstelle einen eigenen temporären Sprachkanal mit oder ohne Benutzerbegrenzung: " +
                "'kit cc [MaxUser/-1/Spielname]'. Spielnamen sind bspw. 'Valorant' oder 'CS:GO'";
    }

    @Override
    public void onExecute(GuildMessageReceivedEvent e, String[] args) {
        Guild guild = Objects.requireNonNull(e.getMember()).getGuild();

        final String CATEGORY_NAME = "User Channels";

        if (guild.getCategoriesByName(CATEGORY_NAME, true).size() == 0) {
            e.getChannel().sendMessage(EmbedUtil.createErrorEmbed().addField("Kategorie nicht gefunden",
                    "Die Kategorie, wo der Kanal erstellt werden soll, existiert nicht. Bitte kontaktiere den " +
                            "Serverbesitzer, um die Kategorie '" + CATEGORY_NAME + "' zu erstellen.", false).build()).queue();
            return;
        }

        Category category = guild.getCategoriesByName(CATEGORY_NAME, true).get(0);

        if (args.length < 1) {
            e.getChannel().sendMessage(EmbedUtil.createInfoEmbed().addField("Benutzungsinformation",
                    "Gebe die Anzahl der Benutzer an die joinen dürfen, einen Spielnamen oder " +
                            "-1 um keine Begrenzung zu setzen.", true).build()).queue();
            return;
        }

        if (Pattern.compile("-?\\d+(\\.\\d+)?").matcher(args[0]).matches()) {
            int i = Integer.parseInt(args[0]);
            if (i == -1) {
                guild.createVoiceChannel(e.getMember().getEffectiveName() + "s Channel", category)
                        .queue(voiceChannel -> channels.add(voiceChannel));
            } else if (i < -1 || i == 0) {
                e.getChannel().sendMessage(EmbedUtil.createErrorEmbed().addField("Begrenzungszahl falsch",
                                "Die Begrenzungszahl ist falsch. Sie darf entweder -1 oder größer als 0 sein.", true)
                        .build()).queue();
            } else {
                guild.createVoiceChannel(e.getMember().getEffectiveName() + "s Channel", category).queue(voiceChannel -> {
                    channels.add(voiceChannel);
                    voiceChannel.getManager().setUserLimit(i).queue();
                });
            }
        } else {
            StringBuilder input = new StringBuilder();
            for (String s : args) {
                input.append(" ").append(s);
            }
            input = new StringBuilder(input.toString().replaceFirst(" ", ""));

            for (String game : games.keySet()) {
                if (game.equalsIgnoreCase(input.toString())) {
                    guild.createVoiceChannel(e.getMember().getEffectiveName() + "s " + game, category).queue(voiceChannel -> {
                        channels.add(voiceChannel);
                        voiceChannel.getManager().setUserLimit(games.get(game)).queue();
                    });
                    return;
                }
            }

            e.getChannel().sendMessage(EmbedUtil.createErrorEmbed().addField("Spiel nicht gefunden",
                            "Das angegebene Spiel konnte nicht gefunden werden", true)
                    .build()).queue();
        }
    }

}
