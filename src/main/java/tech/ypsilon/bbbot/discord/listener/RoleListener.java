package tech.ypsilon.bbbot.discord.listener;

import com.mongodb.client.MongoCollection;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.message.guild.react.GuildMessageReactionAddEvent;
import net.dv8tion.jda.api.events.message.guild.react.GuildMessageReactionRemoveEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.bson.Document;
import org.jetbrains.annotations.NotNull;
import tech.ypsilon.bbbot.database.MongoController;
import tech.ypsilon.bbbot.discord.DiscordController;
import tech.ypsilon.bbbot.discord.command.StudiengangCommand;

import java.util.Objects;

public class RoleListener extends ListenerAdapter {

    /*
    public enum Roles {
        INFORMATICS("\uD83D\uDCBB", 758695532369018910L),
        Wirtschaftsinformatik("\uD83D\uDCB8", 758696116476182528L);
        //Wirtschaftsingeurwesen(":link:", 759034655851675659L),
        //Maschinenbau(":tools:", 759037835759583322L);


        private final String UNICODE;
        private final Role ROLE;

        public String getUnicode() {
            return UNICODE;
        }

        public Role getRole() {
            return ROLE;
        }

        Roles(String unicode, long role) {
            this.UNICODE = unicode;
            this.ROLE = DiscordController.getJDA().getRoleById(role);
        }
    }

    @Override
    public void onReady(@NotNull ReadyEvent event) {
        if(true)
            return;

        String msgS = "Herzlich willkommen auf dem Ersti-Server für's <:KIT:759041596460236822> . Wähle per Klick auf ein Emoji unter der Nachricht deinen Studiengang um die Informationen des Discord-Servers für dich zu personalisieren :star_struck: .\n" +
                ":computer: = Informatik\n" +
                ":money_with_wings: = Wirtschaftsinformatik\n" +
                "\n" +
                "Dein Studiengang fehlt? Schreibe einem Moderator <@&757718320526000138> :100:";

        JDA jda = DiscordController.getJDA();
        Guild guild = jda.getGuildById(756547960229199902L);

        TextChannel text = guild.getTextChannelById(759033520680599553L);
        Message msg = text.sendMessage(msgS).complete();
        for (RoleListener.Roles value : RoleListener.Roles.values()) {
            msg.addReaction(value.getUnicode()).queue();
        }
    }

    private Roles getRole(String unicode) {
        for (Roles value : Roles.values()) {
            if(value.UNICODE.equals(unicode))
                return value;
        }
        return null;
    }

    @Override
    public void onGuildMessageReactionAdd(@NotNull GuildMessageReactionAddEvent event) {
        if(event.getGuild().getIdLong() == 756547960229199902L && event.getChannel().getIdLong() == 759033520680599553L) {
            String unicode = event.getReactionEmote().getEmoji();
            Roles role = getRole(unicode);
            if(role != null) {
                Member member = event.getMember();
                event.getGuild().addRoleToMember(member, role.getRole()).complete();
            }
        }
    }

    @Override
    public void onGuildMessageReactionRemove(@NotNull GuildMessageReactionRemoveEvent event) {
        if(event.getGuild().getIdLong() == 756547960229199902L && event.getChannel().getIdLong() == 759033520680599553L) {
            String unicode = event.getReactionEmote().getEmoji();
            Roles role = getRole(unicode);
            if(role != null) {
                Member member = event.getMember();
                if(member == null) {
                    member = event.getGuild().retrieveMemberById(event.getUserIdLong()).complete();
                }
                event.getGuild().removeRoleFromMember(member, role.getRole()).complete();
            }
        }
    }
    */

    @Override
    public void onGuildMessageReactionAdd(@NotNull GuildMessageReactionAddEvent event) {
        if(event.getMessageIdLong() == 759043590432882798L) {
            MongoCollection<Document> collection = MongoController.getInstance().getCollection("Studiengänge");
            if(collection.countDocuments(new Document("emote", event.getReactionEmote().getEmoji())) > 0){
                Document doc = collection.find(new Document("emote", event.getReactionEmote().getIdLong())).first();

                assert doc != null;
                event.getGuild().addRoleToMember(event.getMember(),
                        Objects.requireNonNull(event.getGuild().getRoleById(doc.getLong("roleId")))).queue();
            }
        }
    }

    @Override
    public void onGuildMessageReactionRemove(@NotNull GuildMessageReactionRemoveEvent event) {
        if(event.getMessageIdLong() == 759043590432882798L  && event.getMember() != null) {
            MongoCollection<Document> collection = MongoController.getInstance().getCollection("Studiengänge");
            if(collection.countDocuments(new Document("emote", event.getReactionEmote().getEmoji())) > 0){
                Document doc = collection.find(new Document("emote", event.getReactionEmote().getIdLong())).first();

                assert doc != null;
                event.getGuild().removeRoleFromMember(Objects.requireNonNull(event.getMember()),
                        Objects.requireNonNull(event.getGuild().getRoleById(doc.getLong("roleId")))).queue();
            }
        }
    }
}
