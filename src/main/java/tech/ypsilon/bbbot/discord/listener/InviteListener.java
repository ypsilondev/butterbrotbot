package tech.ypsilon.bbbot.discord.listener;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Updates;
import net.dv8tion.jda.api.entities.Invite;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.bson.Document;
import org.jetbrains.annotations.NotNull;
import tech.ypsilon.bbbot.ButterBrot;
import tech.ypsilon.bbbot.database.MongoController;

import java.util.List;

public class InviteListener extends ButterbrotListener {

    public InviteListener(ButterBrot parent) {
        super(parent);
    }

    @Override
    public void onGuildMemberJoin(@NotNull GuildMemberJoinEvent event) {
        MongoCollection<Document> collection = MongoController.getInstance().getCollection("Invites");

        List<Invite> invites = event.getGuild().retrieveInvites().complete();
        for (Invite invite : invites) {
            if (collection.countDocuments(new Document("inviteUrl", invite.getUrl())) > 0) {
                Document document = collection.find(new Document("inviteUrl", invite.getUrl())).first();
                assert document != null;

                if(document.getInteger("uses") < invite.getUses()){
                    List<String> roles = document.getList("roles", String.class);
                    collection.updateOne(new Document("inviteUrl", invite.getUrl()), Updates.inc("uses", 1));

                    for (String roleId : roles) {
                        Role role = event.getGuild().getRoleById(roleId);
                        assert role != null;
                        event.getGuild().addRoleToMember(event.getMember(), role).queue();
                    }
                }

                break;
            }
        }
    }
}
