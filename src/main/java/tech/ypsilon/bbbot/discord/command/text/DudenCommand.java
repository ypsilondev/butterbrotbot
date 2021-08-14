package tech.ypsilon.bbbot.discord.command.text;

import me.gregyyy.jduden.JDuden;
import me.gregyyy.jduden.Word;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.events.message.priv.PrivateMessageReceivedEvent;
import org.jsoup.HttpStatusException;
import tech.ypsilon.bbbot.ButterBrot;
import tech.ypsilon.bbbot.util.EmbedUtil;

import java.io.IOException;

@Deprecated
public class DudenCommand extends FullStackedExecutor {

    @Override
    public String[] getAlias() {
        return new String[]{"duden"};
    }

    @Override
    public String getDescription() {
        return "Wörter im Online-Duden nachschlagen: 'kit duden [Wort]'";
    }

    @Override
    public void onExecute(GuildMessageReceivedEvent e, String[] args) {
        answer(e.getChannel(), args);
        e.getMessage().delete().queue();
    }

    @Override
    public void onPrivateExecute(PrivateMessageReceivedEvent e, String[] args) {
        answer(e.getChannel(), args);
    }

    private void answer(MessageChannel channel, String[] args){
        if(args.length < 1){
            channel.sendMessage(EmbedUtil.createErrorEmbed()
                    .addField("Fehlendes Argument", "Du musst ein Wort eingeben", false).build()).queue();
            return;
        }

        StringBuilder input = new StringBuilder();
        for(String s : args){
            input.append(s).append(" ");
        }
        input = new StringBuilder(input.toString().trim());

        if(!input.toString().matches("^[A-Za-zÖÄÜöäüßẞ -]+$")){
            channel.sendMessage(EmbedUtil.createErrorEmbed()
                    .addField("Fehlendes Argument", "Dein Wort einhält nicht erlaubte Zeichen", false).build()).queue();
            return;
        }

        try{
            Word word = JDuden.getWord(input.toString());

            EmbedBuilder embed = EmbedUtil.createDefaultEmbed();
            embed.setTitle("Duden Rechtschreibung");

            embed.addField("Wort", word.getWord(), true);
            if(word.getAltSpellings() != null) embed.addField("Alternative Schreibweise", word.getAltSpellings(), true);

            StringBuilder articles = new StringBuilder();
            for(String article : word.getArticles()){
                articles.append(", ").append(article);
            }
            articles = new StringBuilder(articles.toString().replaceFirst(", ", " "));
            if(word.getArticles() != null) embed.addField("Artikel", articles.toString(), true);

            if(word.getWordType() != null) embed.addField("Wortart", word.getWordType(), true);
            if(word.getWordSeparation() != null)  embed.addField("Worttrennung", word.getWordSeparation(), true);

            StringBuilder meaning = new StringBuilder();
            if(word.getWord().equals("Butterbrot")){
                meaning.append("\n- ein Stück Software");
            }else{
                for(String m : word.getMeanings()){
                    meaning.append("\n- ").append(m);
                }
            }
            meaning = new StringBuilder(meaning.toString().replaceFirst("\n", ""));
            if(!meaning.toString().isEmpty()) embed.addField(meaning.toString().contains("\n") ? "Bedeutungen" : "Bedeutung",
                    meaning.toString(), false);

            if(word.getOrigin() != null) embed.addField("Herkunft", word.getOrigin(), true);

            channel.sendMessage(embed.build()).queue();
        }catch(HttpStatusException ex){
            if(ex.getStatusCode() == 404){
                channel.sendMessage(EmbedUtil.createErrorEmbed()
                        .addField("Wort nicht gefunden", "Das eingegebene Wort wurde nicht gefunden " +
                                "(Tipp: Achte auf Groß- und Kleinschreibung)", false)
                        .build()).queue();
            }else{
                ButterBrot.LOGGER.error("Error while lookup up a word on duden.de", ex);
                channel.sendMessage(EmbedUtil.createErrorEmbed()
                        .addField("Fehler", "Ein Fehler ist aufgetreten", false).build()).queue();
            }
        }catch(IOException ex){
            ButterBrot.LOGGER.error("Error while lookup up a word on duden.de", ex);
            channel.sendMessage(EmbedUtil.createErrorEmbed()
                    .addField("Fehler", "Ein Fehler ist aufgetreten", false).build()).queue();
        }
    }
}
