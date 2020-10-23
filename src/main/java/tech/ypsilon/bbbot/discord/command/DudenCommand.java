package tech.ypsilon.bbbot.discord.command;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import tech.ypsilon.bbbot.ButterBrot;
import tech.ypsilon.bbbot.util.EmbedUtil;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

public class DudenCommand extends LegacyCommand {

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
        if(args.length < 1){
            e.getChannel().sendMessage(EmbedUtil.createErrorEmbed()
                    .addField("Fehlendes Argument", "Du musst ein Wort eingeben", false).build()).queue();
            return;
        }

        StringBuilder input = new StringBuilder();
        for(String s : args){
            input.append(s).append(" ");
        }
        input = new StringBuilder(input.toString().trim());
        input = new StringBuilder(input.toString().replaceAll("Ä", "Ae").replaceAll("Ö", "Oe")
                .replaceAll("Ü", "Ue").replaceAll("ä", "ae").replaceAll("ö", "oe")
                .replaceAll("ü", "üe").replaceAll("ß", "sz"));

        if(!input.toString().matches("^[A-Za-z -]+$")){
            e.getChannel().sendMessage(EmbedUtil.createErrorEmbed()
                    .addField("Fehlendes Argument", "Dein Wort einhält nicht erlaubte Zeichen", false).build()).queue();
            return;
        }

        try{
            Word word = getWord(input.toString());

            EmbedBuilder embed = EmbedUtil.createDefaultEmbed();
            embed.setTitle("Duden Rechtschreibung");

            embed.addField("Wort", word.getWord(), true);
            if(word.getAltSpellings() != null) embed.addField("Alternative Schreibweise", word.getAltSpellings(), true);
            if(word.getArticles() != null) embed.addField("Artikel", word.getArticles(), true);
            if(word.getWordType() != null) embed.addField("Wortart", word.getWordType(), true);
            if(word.getWordSeparation() != null)  embed.addField("Worttrennung", word.getWordSeparation(), true);

            StringBuilder meaning = new StringBuilder();
            for(String m : word.getMeanings()){
                meaning.append("\n- ").append(m);
            }
            meaning = new StringBuilder(meaning.toString().replaceFirst("\n", ""));
            if(!meaning.toString().isEmpty()) embed.addField(meaning.toString().contains("\n") ? "Bedeutungen" : "Bedeutung",
                    meaning.toString(), false);

            if(word.getOrigin() != null) embed.addField("Herkunft", word.getOrigin(), true);

            e.getChannel().sendMessage(embed.build()).queue();
        }catch(HttpStatusException ex){
            if(ex.getStatusCode() == 404){
                e.getChannel().sendMessage(EmbedUtil.createErrorEmbed()
                        .addField("Wort nicht gefunden", "Das eingegebene Wort wurde nicht gefunden " +
                                "(Tipp: Achte auf Groß- und Kleinschreibung)", false)
                        .build()).queue();
            }else{
                ButterBrot.LOGGER.error("Error while lookup up a word on duden.de", ex);
                e.getChannel().sendMessage(EmbedUtil.createErrorEmbed()
                        .addField("Fehler", "Ein Fehler ist aufgetreten", false).build()).queue();
            }
        }catch(IOException ex){
            ButterBrot.LOGGER.error("Error while lookup up a word on duden.de", ex);
            e.getChannel().sendMessage(EmbedUtil.createErrorEmbed()
                    .addField("Fehler", "Ein Fehler ist aufgetreten", false).build()).queue();
        }
    }

    public static Word getWord(String word) throws IOException {
        Document doc = Jsoup.connect("https://www.duden.de/rechtschreibung/" + word)
                .userAgent("Mozilla/5.0 (Windows; U; WindowsNT 88.69; en-US; rv1.8.1.6) Gecko/20070725 Firefox/2.0.0.6").get();

        String dWord = getElementByClassOrDefault(doc, "lemma__main", null);
        String altSpellings = getElementByClassOrDefault(doc, "lemma__alt-spelling", null);
        String articles = getElementByClassOrDefault(doc, "lemma__determiner", null);
        String wordType = getElementByCSSOrDefault(doc,
                ".tabloid__main-column > article:nth-child(3) > dl:nth-child(3) > dd:nth-child(2)", null);

        String wordSeparation = null;
        for(Element element : doc.getElementById("rechtschreibung").children()){
            if(element.className().equals("tuple")){
                boolean valueNext = false;
                for(Element element1 : element.children()){
                    if(element1.className().equals("tuple__key") && element1.text().equals("Worttrennung")){
                        valueNext = true;
                    }else if(valueNext){
                        wordSeparation = element1.text();
                        break;
                    }
                }
            }
        }

        List<String> meanings = new ArrayList<>();
        if(doc.getElementById("bedeutung") != null){
            doc.getElementById("bedeutung").children().stream().filter(element -> element.nodeName().equals("p"))
                    .findFirst().ifPresent(element -> meanings.add(element.text()));
        }else{
            doc.getElementById("bedeutungen").children().stream().filter(element -> element.className().equals("enumeration")).findFirst().ifPresent(element -> {
                for(Element items : element.children()){
                    for(Element entries : items.children()){
                        if(entries.className().equals("enumeration__text")){
                            meanings.add(entries.text());
                        }else if(entries.className().equals("enumeration__sub")){
                            for(Element subitems : entries.children()){
                                for(Element subitemsEntries : subitems.children()){
                                    if(subitemsEntries.className().equals("enumeration__text")) {
                                        meanings.add(subitemsEntries.text());
                                    }
                                }
                            }
                        }
                    }
                }
            });
        }

        AtomicReference<String> origin = new AtomicReference<>();
        if(doc.getElementById("herkunft") != null){
            doc.getElementById("herkunft").children().stream().filter(element -> element.nodeName().equals("p")).findFirst().ifPresent(element -> origin.set(element.text()));
        }

        return new Word(dWord, altSpellings, articles, wordType, wordSeparation, meanings, origin.get());
    }

    private static String getElementByClassOrDefault(Document doc, String className, String defaultValue){
        if(doc.body().getElementsByClass(className).size() > 0){
            return doc.body().getElementsByClass(className).get(0).text();
        }else{
            return defaultValue;
        }
    }

    private static String getElementByCSSOrDefault(Document doc, String cssPath, String defaultValue){
        if(doc.body().select(cssPath).size() > 0){
            return doc.body().select(cssPath).get(0).text();
        }else{
            return defaultValue;
        }
    }


    static class Word {

        private final String word;
        private final String altSpellings;
        private final String articles;
        private final String wordType;
        private final String wordSeparation;
        private final List<String> meanings;
        private final String origin;

        public Word(String word, String altSpellings, String articles, String wordType, String wordSeparation,
                    List<String> meanings, String origin) {
            this.word = word;
            this.altSpellings = altSpellings;
            this.articles = articles;
            this.wordType = wordType;
            this.wordSeparation = wordSeparation;
            this.meanings = meanings;
            this.origin = origin;
        }

        public String getWord() {
            return word;
        }

        public String getAltSpellings() {
            return altSpellings;
        }

        public String getArticles() {
            return articles;
        }

        public String getWordType() {
            return wordType;
        }

        public String getWordSeparation() {
            return wordSeparation;
        }

        public List<String> getMeanings() {
            return meanings;
        }

        public String getOrigin() {
            return origin;
        }

        @Override
        public String toString() {
            return "Word{" +
                    "word='" + word + '\'' +
                    ", altSpellings='" + altSpellings + '\'' +
                    ", articles='" + articles + '\'' +
                    ", wordType='" + wordType + '\'' +
                    ", wordSeparation='" + wordSeparation + '\'' +
                    ", meanings=" + meanings +
                    ", origin='" + origin + '\'' +
                    '}';
        }

    }

}
