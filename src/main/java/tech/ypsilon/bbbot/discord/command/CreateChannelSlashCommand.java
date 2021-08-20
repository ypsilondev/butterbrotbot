package tech.ypsilon.bbbot.discord.command;

import net.dv8tion.jda.api.entities.Category;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.VoiceChannel;
import net.dv8tion.jda.api.events.interaction.SelectionMenuEvent;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.selections.SelectionMenu;
import org.jetbrains.annotations.Nullable;
import tech.ypsilon.bbbot.util.EmbedUtil;

import java.util.*;
import java.util.stream.Collectors;

public class CreateChannelSlashCommand extends SlashCommand {

    public static final List<VoiceChannel> channels = new ArrayList<>();

    @Override
    public CommandData commandData() {
        return new CommandData("create-channel", "Erstellt einen neuen temporären Sprachkanal")
                .addSubcommands(
                        new SubcommandData("game", "Startet einen Dialog zum erstellen eines neuen Sprachkanals für ein bestimmtes Spiel"),
                        new SubcommandData("custom", "Lässt dich den Kanal frei konfigurieren.").addOptions(
                                new OptionData(OptionType.STRING, "name", "Name des Kanals", true),
                                new OptionData(OptionType.INTEGER, "limit", "Anzahl der Benutzer (-1 für kein Limit)", true)
                        )
                );
    }

    @Override
    public @Nullable String getHelpDescription() {
        return "/create-channel game startet einen Dialog zum erstellen eines neuen temporären Sprachkanals für ein bestimmtes Spiel.\n" +
                "/create-channel custom <name> <limit> erstellt einen temporären Sprachkanal mit dem Namen <name> und dem Nutzerlimit <limit>";
    }

    @Override
    public void execute(SlashCommandEvent event) {
        event.deferReply(true).queue();

        if (event.getSubcommandName() == null) {
            throw new CommandFailedException("No subcommand provided");
        }

        if (event.getSubcommandName().equalsIgnoreCase("custom")) {
            String name = Objects.requireNonNull(event.getOption("name")).getAsString();
            name += " von " + Objects.requireNonNull(event.getMember()).getEffectiveName();
            int maxUsers = (int) Objects.requireNonNull(event.getOption("limit")).getAsLong();
            this.createChannel(name, maxUsers, Objects.requireNonNull(event.getGuild()), event.getHook());
        } else if (event.getSubcommandName().equalsIgnoreCase("game")) {
            SelectionMenu.Builder menuBuilder = SelectionMenu.create(createSelectMenuId("create-channel-menu"));

            menuBuilder.setPlaceholder("Wähle ein Spiel aus");

            PrivateChannelType.getChannelTypes().forEach((name, count) -> {
                menuBuilder.addOption(String.format("%s (%d Spieler)", name, count), name);
            });

            event.getHook().editOriginal("Bitte wähle ein Spiel aus:").queue();
            event.getHook().editOriginalComponents(ActionRow.of(menuBuilder.build())).queue();
        }


    }

    @Override
    public void handleSelectionMenu(SelectionMenuEvent event, String data) {
        event.deferReply(true).queue();
        String channelName = Objects.requireNonNull(event.getSelectedOptions()).get(0).getValue();
        int maxUsers = PrivateChannelType.getChannelTypes().get(channelName);
        channelName += " von " + Objects.requireNonNull(event.getMember()).getEffectiveName();


        this.createChannel(channelName, maxUsers, Objects.requireNonNull(event.getGuild()), event.getHook());
    }

    private void createChannel(String name, int maxUsers, Guild guild, InteractionHook hook) {

        final String CATEGORY_NAME = "User Channels";

        if (guild.getCategoriesByName(CATEGORY_NAME, true).size() == 0) {
            hook.editOriginalEmbeds(EmbedUtil.createErrorEmbed().addField("Kategorie nicht gefunden",
                    "Die Kategorie, wo der Kanal erstellt werden soll, existiert nicht. Bitte kontaktiere den " +
                            "Serverbesitzer, um die Kategorie '" + CATEGORY_NAME + "' zu erstellen.", false).build()).queue();
            return;
        }

        Category category = guild.getCategoriesByName(CATEGORY_NAME, true).get(0);

        if (name.length() > 0) {
            if (maxUsers == -1) {
                guild.createVoiceChannel(name, category).queue(channels::add);
            } else if (maxUsers > 0) {
                guild.createVoiceChannel(name, category).queue(voiceChannel -> {
                    channels.add(voiceChannel);
                    voiceChannel.getManager().setUserLimit(maxUsers).queue();
                });
            }
        }

        hook.editOriginalEmbeds(EmbedUtil.createSuccessEmbed().addField("Kanal erstellt", "Der Kanal wurde erfolgreich erstellt und ist jetzt unter " + CATEGORY_NAME + " zu finden.", false).build()).queue();
    }

    private enum PrivateChannelType {
        AMONG_US("Among us", 10),
        SCHACH("Schach", 2),
        OVERWATCH("Overwatch", 6),
        LEAGUE_OF_LEGENDS("League of Legends", 5),
        COOP("Coop", 2),
        RAINBOW_SIX("Rainbow Six Siege", 5),
        CSGO("CS:GO", 5),
        ROCKET_LEAGUE("Rocket League", 3),
        DEAD_BY_DAYLIGHT("Dead by Daylight", 5),
        APEX_LEGENDS("Apex Legends", 3),
        THE_DIVISION_TWO("The Division 2", 4),
        FOR_HONOR("For Honor", 4),
        GHOST_RECON("Ghost Recon", 4),
        USO("Uso", 8),
        STRONGHOLD("Stronghold", 4),
        DESTINY_TWO("Destiny 2", 4),
        VALORANT("Valorant", 5),
        MINECRAFT("Minecraft", 15);


        private final String name;
        private final int maxUsers;

        PrivateChannelType(String name, int maxUsers) {
            this.name = name;
            this.maxUsers = maxUsers;
        }

        public static PrivateChannelType getChannelByName(String name) {
            return Arrays.stream(PrivateChannelType.values())
                    .filter(type -> type.getName().equalsIgnoreCase(name))
                    .findFirst()
                    .orElseThrow(() -> new CommandFailedException("Channel type not found :c"));
        }

        public static Map<String, Integer> getChannelTypes() {
            return Arrays.stream(PrivateChannelType.values())
                    .collect(Collectors.toMap(PrivateChannelType::getName, PrivateChannelType::getMaxUsers));
        }

        public String getName() {
            return name;
        }

        public int getMaxUsers() {
            return maxUsers;
        }
    }
}
