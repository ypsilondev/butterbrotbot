package tech.ypsilon.bbbot.discord.command;

import me.gregyyy.jduden.JDuden;
import me.gregyyy.jduden.Word;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import org.jetbrains.annotations.Nullable;
import org.jsoup.HttpStatusException;
import tech.ypsilon.bbbot.ButterBrot;
import tech.ypsilon.bbbot.util.EmbedUtil;

import java.io.IOException;
import java.util.Objects;

public class DudenSlashCommand extends SlashCommand {
    @Override
    public CommandData commandData() {
        return new CommandData("duden", "Wörter im Online-Duden nachschlagen: 'kit duden [Wort]'").addOptions(
                new OptionData(OptionType.STRING, "wort", "Das nachzuschlagende Wort", true)
        );
    }

    @Override
    public @Nullable String getHelpDescription() {
        return "/duden <wort> sucht das Wort im Duden";
    }

    @Override
    public boolean isGlobal() {
        return true;
    }

    @Override
    public void execute(SlashCommandEvent event) {
        event.deferReply(false).queue();
        String input = Objects.requireNonNull(event.getOption("wort")).getAsString();


        if (!input.matches("^[A-Za-zÖÄÜöäüßẞ -]+$")) {
            event.getHook().editOriginalEmbeds(EmbedUtil.createErrorEmbed()
                    .addField("Fehlendes Argument", "Dein Wort einhält nicht erlaubte Zeichen", false).build()).queue();
            return;
        }

        try {
            Word word = JDuden.getWord(input);

            EmbedBuilder embed = EmbedUtil.createDefaultEmbed();
            embed.setTitle("Duden Rechtschreibung");

            embed.addField("Wort", word.getWord(), true);
            if (word.getAltSpellings() != null)
                embed.addField("Alternative Schreibweise", word.getAltSpellings(), true);

            StringBuilder articles = new StringBuilder();
            for (String article : word.getArticles()) {
                articles.append(", ").append(article);
            }
            articles = new StringBuilder(articles.toString().replaceFirst(", ", " "));
            if (word.getArticles() != null) embed.addField("Artikel", articles.toString(), true);

            if (word.getWordType() != null) embed.addField("Wortart", word.getWordType(), true);
            if (word.getWordSeparation() != null) embed.addField("Worttrennung", word.getWordSeparation(), true);

            StringBuilder meaning = new StringBuilder();
            if (word.getWord().equals("Butterbrot")) {
                meaning.append("\n- ein Stück Weichware");
            } else {
                for (String m : word.getMeanings()) {
                    meaning.append("\n- ").append(m);
                }
            }
            meaning = new StringBuilder(meaning.toString().replaceFirst("\n", ""));
            if (!meaning.toString().isEmpty())
                embed.addField(meaning.toString().contains("\n") ? "Bedeutungen" : "Bedeutung",
                        meaning.toString(), false);

            if (word.getOrigin() != null) embed.addField("Herkunft", word.getOrigin(), true);

            event.getHook().editOriginalEmbeds(embed.build()).queue();
        } catch (HttpStatusException ex) {
            if (ex.getStatusCode() == 404) {
                event.getHook().editOriginalEmbeds(EmbedUtil.createErrorEmbed()
                        .addField("Wort nicht gefunden", "Das eingegebene Wort wurde nicht gefunden " +
                                "(Tipp: Achte auf Groß- und Kleinschreibung)", false)
                        .build()).queue();
            } else {
                ButterBrot.LOGGER.error("Error while lookup up a word on duden.de", ex);
                event.getHook().editOriginalEmbeds(EmbedUtil.createErrorEmbed()
                        .addField("Fehler", "Ein Fehler ist aufgetreten", false).build()).queue();
            }
        } catch (IOException ex) {
            ButterBrot.LOGGER.error("Error while lookup up a word on duden.de", ex);
            event.getHook().editOriginalEmbeds(EmbedUtil.createErrorEmbed()
                    .addField("Fehler", "Ein Fehler ist aufgetreten", false).build()).queue();
        }
    }

}
