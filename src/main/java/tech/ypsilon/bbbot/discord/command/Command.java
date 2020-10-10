package tech.ypsilon.bbbot.discord.command;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import tech.ypsilon.bbbot.util.EmbedUtil;

import java.util.regex.Pattern;

public abstract class Command implements DiscordFunction {

    public abstract String[] getAlias();
    public abstract void onExecute(GuildMessageReceivedEvent e, String[] args);
    public abstract String getDescription();

    /**
     * Easy way to check which arguments the user inputted
     *
     * @param i         Index of argument to check
     * @param args      Arguments the user has inputted
     * @param things    Available arguments
     * @param e         Event for send error messages
     * @return          The argument the user inputted or an error message
     */
    protected final String checkArgs(int i, String[] args, String[] things, GuildMessageReceivedEvent e){
        StringBuilder array = new StringBuilder();
        for(String s : things) array.append(", ").append(s);
        array = new StringBuilder(array.toString().replaceFirst(Pattern.quote(", "), ""));

        if(args.length <= i){
            EmbedBuilder embedBuilder = EmbedUtil.createInfoEmbed();
            embedBuilder.addField("Verfügbare Argumente:", array.toString(), false);
            e.getChannel().sendMessage(embedBuilder.build()).queue();
            return "args to short";
        }

        for(String s : things) if(args[i].equalsIgnoreCase(s)) return s;

        EmbedBuilder embedBuilder = EmbedUtil.createErrorEmbed();
        embedBuilder.addField("Argument nicht gefunden. Verfügbare Argumente:", array.toString(), false);
        return "not found";
    }

}
