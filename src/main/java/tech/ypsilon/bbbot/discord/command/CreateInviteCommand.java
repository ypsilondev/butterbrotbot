package tech.ypsilon.bbbot.discord.command;

import com.mongodb.client.MongoCollection;
import net.dv8tion.jda.api.entities.ISnowflake;
import net.dv8tion.jda.api.entities.Invite;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import org.bson.Document;
import org.bson.types.ObjectId;
import tech.ypsilon.bbbot.database.MongoController;
import tech.ypsilon.bbbot.util.DiscordUtil;
import tech.ypsilon.bbbot.util.EmbedUtil;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

public class CreateInviteCommand implements GuildExecuteHandler {

    final MongoCollection<Document> collection = MongoController.getInstance().getCollection("Invites");

    @Override
    public String[] getAlias() {
        return new String[]{"createinvite", "cinvite"};
    }

    @Override
    public String getDescription() {
        return "Create invites that give roles automatically";
    }


    @Override
    public void onExecute(GuildMessageReceivedEvent e, String[] args) {
        if (!DiscordUtil.isAdmin(Objects.requireNonNull(e.getMember()))) {
            e.getChannel().sendMessage(EmbedUtil.createErrorEmbed().addField("Kein Recht",
                    "Du hast kein Recht diesen Befehl auszuführen", false).build()).queue();
            return;
        }

        if (e.getMessage().getMentionedRoles().size() == 0) {
            e.getChannel().sendMessage(EmbedUtil.createErrorEmbed().addField("Keine Rolle angegeben",
                    "Du musst mindestens eine Rolle angeben, damit ein Einladungslink erstellt werden kann.",
                    false).build()).queue();
            return;
        }

        List<String> roleIds = e.getMessage().getMentionedRoles().stream().map(ISnowflake::getId).collect(Collectors.toList());

        if (collection.countDocuments(new Document("roles", roleIds)) > 0) {
            String url = Objects.requireNonNull(collection.find(new Document("roles", roleIds)).first()).getString("inviteUrl");
            e.getChannel().sendMessage(EmbedUtil.createErrorEmbed().addField("Link existiert bereits",
                    "Es existiert bereits ein Link für diese Rollen. Link: " + url, false).build()).queue();
            return;
        }

        List<Invite> invites = e.getGuild().retrieveInvites().complete();
        Optional<Invite> optional = invites.stream().filter(i -> Objects.requireNonNull(i.getChannel()).getId().equals(e.getChannel().getId()) &&
                Objects.requireNonNull(i.getInviter()).getId().equals(e.getJDA().getSelfUser().getId())).findFirst();
        int uses = optional.map(Invite::getUses).orElse(0);

        Invite invite = e.getChannel().createInvite().setMaxAge(0).setMaxUses(0).complete();

        collection.insertOne(new Document("_id", new ObjectId()).append("roles", roleIds)
                .append("inviteUrl", invite.getUrl()).append("uses", uses));

        e.getChannel().sendMessage(EmbedUtil.createSuccessEmbed().addField("Einladungslink erstellt", "Der " +
                "Einladungslink wurde erfolgreich erstellt. Link: " + invite.getUrl(), true).build()).queue();

    }

}
