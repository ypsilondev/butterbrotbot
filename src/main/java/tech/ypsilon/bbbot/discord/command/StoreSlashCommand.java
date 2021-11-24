package tech.ypsilon.bbbot.discord.command;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import tech.ypsilon.bbbot.ButterBrot;
import tech.ypsilon.bbbot.database.codecs.LinkCodec;
import tech.ypsilon.bbbot.util.EmbedUtil;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class StoreSlashCommand extends SlashCommand {

    public StoreSlashCommand(ButterBrot parent) {
        super(parent);
    }

    @Override
    public CommandData commandData() {
        return new CommandData("store", "Speichert Links").addOptions(
                new OptionData(OptionType.STRING, "name", "Der Name des Links", true),
                new OptionData(OptionType.STRING, "link", "Der Link", true),
                new OptionData(OptionType.STRING, "keywords", "Keywords", false)
        );
    }

    @Override
    public void execute(SlashCommandEvent event) {
        event.deferReply(true).queue();

        String name = Objects.requireNonNull(event.getOption("name")).getAsString();
        String link = Objects.requireNonNull(event.getOption("link")).getAsString();
        String keywordString = "";
        if (event.getOption("keywords") != null) {
            keywordString = Objects.requireNonNull(event.getOption("keywords")).getAsString();
        }

        if (LinkCodec.isPresent(name, link)) {
            EmbedBuilder b = EmbedUtil.createErrorEmbed();
            b.setDescription("Name oder Link schon vorhanden");
            event.getHook().editOriginalEmbeds(b.build()).queue();
            return;
        }

        List<String> keywords = Arrays.asList(keywordString.split(" "));

        LinkCodec.createLink(name, event.getUser(), link, keywords);

        EmbedBuilder b = EmbedUtil.createSuccessEmbed();
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("Dein Link wurde erfolgreich mit dem Namen '").append(name).append("' ");

        if (keywords.size() == 1) {
            stringBuilder.append("und dem Keyword ").append(keywords.get(0));
        } else if (keywords.size() > 1) {
            stringBuilder.append("und den Keywords ").append(String.join(", ", keywords));
        }
        stringBuilder.append(" verknüpft");
        b.setDescription(stringBuilder.toString());
        event.getHook().editOriginalEmbeds(b.build()).queue();

    }
}
