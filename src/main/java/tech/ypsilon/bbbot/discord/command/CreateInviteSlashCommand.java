package tech.ypsilon.bbbot.discord.command;

import com.mongodb.client.MongoCollection;
import net.dv8tion.jda.api.entities.GuildChannel;
import net.dv8tion.jda.api.entities.Invite;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import org.bson.Document;
import org.bson.types.ObjectId;
import tech.ypsilon.bbbot.database.MongoController;
import tech.ypsilon.bbbot.util.DiscordUtil;
import tech.ypsilon.bbbot.util.EmbedUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class CreateInviteSlashCommand extends SlashCommand {

    MongoCollection<Document> collection = null;

    private static final int MAX_ROLES = 6;

    @Override
    public CommandData commandData() {
        CommandData data = new CommandData("create-invite", "Erstellt Einladungen, die automatisch Rollen zuweisen").addOptions(
                new OptionData(OptionType.ROLE, "rolle", "Die Rolle, die dem Benutzer zugewiesen werden soll.", true),
                new OptionData(OptionType.CHANNEL, "kanal", "Der Kanal, auf den der Invite führen soll", false)
        );

        // Add additional parameters to allow multiple roles.
        for (int i = 2; i < MAX_ROLES; i++) {
            data.addOption(OptionType.ROLE, "rolle" + i, "Weitere Rollen für die Benutzer");
        }

        return data;
    }

    @Override
    public boolean isGlobal() {
        return false;
    }

    @Override
    public void execute(SlashCommandEvent event) {
        event.deferReply(true).queue();
        if (!DiscordUtil.isAdmin(Objects.requireNonNull(event.getMember()))) {
            event.getHook().editOriginalEmbeds(EmbedUtil.createNoPermEmbed().build()).queue();
            return;
        }

        if (!event.isFromGuild()) {
            event.getHook().editOriginalEmbeds(EmbedUtil.createErrorEmbed().addField("Falscher Kanal", "Dieser Befehl kann nur auf einer Guild benutzt werden!", true).build()).queue();
            return;
        }

        Role role = Objects.requireNonNull(event.getOption("rolle")).getAsRole();
        List<Long> roleIds = new ArrayList<>();
        roleIds.add(role.getIdLong());

        for (int i = 2; i < MAX_ROLES; i++) {
            OptionMapping mapping;
            if ((mapping = event.getOption("rolle" + i)) != null) {
                roleIds.add(mapping.getAsRole().getIdLong());
            }
        }

        if (this.getCollection().countDocuments(new Document("roles", roleIds)) > 0) {
            String url = Objects.requireNonNull(this.getCollection().find(new Document("roles", roleIds)).first()).getString("inviteUrl");
            event.getHook().editOriginalEmbeds(EmbedUtil.createErrorEmbed().addField("Link existiert bereits",
                    "Es existiert bereits ein Link für diese Rollen. Link: " + url, false).build()).queue();
            return;
        }

        GuildChannel channel = event.getGuildChannel();
        if (event.getOption("kanal") != null) {
            channel = Objects.requireNonNull(event.getOption("kanal")).getAsGuildChannel();
        }

        assert event.getGuild() != null;
        List<Invite> invites = event.getGuild().retrieveInvites().complete();

        GuildChannel finalChannel = channel; // Because of Java.... :c
        Optional<Invite> optional = invites.stream().filter(i -> Objects.requireNonNull(i.getChannel()).getId().equals(finalChannel.getId()) &&
                Objects.requireNonNull(i.getInviter()).getId().equals(event.getJDA().getSelfUser().getId())).findFirst();

        int uses = optional.map(Invite::getUses).orElse(0);

        Invite invite = channel.createInvite().setMaxAge(0).setMaxUses(0).complete();

        this.getCollection().insertOne(new Document("_id", new ObjectId()).append("roles", roleIds)
                .append("inviteUrl", invite.getUrl()).append("uses", uses));

        event.getHook().editOriginalEmbeds(EmbedUtil.createSuccessEmbed().addField("Einladungslink erstellt", "Der " +
                "Einladungslink wurde erfolgreich erstellt. Link: " + invite.getUrl(), true).build()).queue();

    }

    public MongoCollection<Document> getCollection() {
        if (this.collection == null) {
            this.collection = MongoController.getInstance().getCollection("Invites");
        }
        return this.collection;
    }
}
