package tech.ypsilon.bbbot.discord.command;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import tech.ypsilon.bbbot.ButterBrot;
import tech.ypsilon.bbbot.database.codecs.DirectoryCodec;
import tech.ypsilon.bbbot.database.codecs.LinkCodec;
import tech.ypsilon.bbbot.util.EmbedUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * This code does basically stuff....
 *
 * @author originally yNiklas, ported by Shirkanesi. Please don't blame either of us...
 * @version 1.0
 * @since 1.4.0
 */
public class DirectorySlashCommand extends SlashCommand {
    public DirectorySlashCommand(ButterBrot parent) {
        super(parent);
    }

    @Override
    public CommandData commandData() {
        List<OptionData> links = new ArrayList<>();
        for (int i = 2; i <= 6; i++) {
            links.add(new OptionData(OptionType.STRING, "link-" + i, "Link " + i));
        }

        return new CommandData("directory", "IDK?!").addSubcommands(
                new SubcommandData("get", "???").addOptions(
                        new OptionData(OptionType.STRING, "dir-name", "Name der Sammlung", true)
                ),
                new SubcommandData("add", "Fügt eine neue Sammlung hinzu.").addOptions(
                        new OptionData(OptionType.STRING, "dir-name", "Name der Sammlung", true),
                        new OptionData(OptionType.BOOLEAN, "public", "Öffentlich?", true)
                ),
                new SubcommandData("edit", "Bearbeitet eine Sammlung").addOptions(
                        new OptionData(OptionType.STRING, "dir-name", "Name der Sammlung", true),
                        new OptionData(OptionType.BOOLEAN, "add", "Hinzufügen / Entfernen", true),
                        new OptionData(OptionType.STRING, "link-1", "Link 1", true)
                ).addOptions(links)
        );
    }

    @Override
    public void execute(SlashCommandEvent event) {
        event.deferReply(true).queue();

        switch (Objects.requireNonNull(event.getSubcommandName())) {
            case "add":
                handleAddDir(event);
                break;
            case "edit":
                handleEditDir(event);
                break;
            case "get":
                handleGetDir(event);
                break;
            default:
                event.getHook().editOriginalEmbeds(EmbedUtil.createErrorEmbed().setTitle("Fehler").setDescription("Unbekannter subcommand-type :c").build()).queue();
                break;
        }
    }

    private void handleEditDir(SlashCommandEvent event) {
        OptionMapping dirNameMapping = event.getOption("dir-name");
        OptionMapping addMapping = event.getOption("add");
        List<OptionMapping> linkMappings = new ArrayList<>();
        for (int i = 1; i <= 6; i++) {
            linkMappings.add(event.getOption("link-" + i));
        }

        List<String> links = new ArrayList<>();
        linkMappings.forEach(mapping -> {
            if (mapping != null) {
                links.add(mapping.getAsString());
            }
        });

        String dirName = Objects.requireNonNull(dirNameMapping).getAsString();

        DirectoryCodec directory = DirectoryCodec.getDirectory(event.getUser(), dirName);
        if (directory == null) {
            EmbedBuilder b = EmbedUtil.createErrorEmbed();
            b.setDescription("Keine Sammlung zu diesem Namen gefunden");
            event.getHook().editOriginalEmbeds(b.build()).queue();
            return;
        }

        boolean isAdd = Objects.requireNonNull(addMapping).getAsBoolean();

        List<LinkCodec> compatibleNames = new ArrayList<>();
        List<String> incompatibleNames = new ArrayList<>();
        if (isAdd) {
            for (String link : links) {
                boolean found = false;
                for (LinkCodec linkCodec : LinkCodec.getLinksForName("^" + link + "$")) {
                    compatibleNames.add(linkCodec);
                    found = true;
                }
                if (!found) incompatibleNames.add(link);
            }

            if (compatibleNames.size() > 0) {
                EmbedBuilder b = EmbedUtil.createSuccessEmbed();
                b.setDescription("Folgende Verknüpfungen wurden hinzugefügt:");
                for (LinkCodec compatibleName : compatibleNames) {
                    directory.addLink(compatibleName);
                    b.addField(compatibleName.getName(), compatibleName.getLink(), false);
                }
                event.getHook().editOriginalEmbeds(b.build()).queue();
            }

            if (incompatibleNames.size() > 0) {
                EmbedBuilder b = EmbedUtil.createErrorEmbed();
                b.setDescription("Folgende Verknüpfungen konnten nicht gefunden und hinzugefügt werden:");
                for (String incompatibleName : incompatibleNames) {
                    b.addField(incompatibleName, "", false);
                }
                event.getHook().editOriginalEmbeds(b.build()).queue();
            }
        } else {
            for (String link : linkMappings.stream().map(OptionMapping::getAsString).collect(Collectors.toList())) {
                boolean found = false;
                for (LinkCodec linkCodec : LinkCodec.getLinksForName("^" + link + "$")) {
                    compatibleNames.add(linkCodec);
                    found = true;
                }
                if (!found) incompatibleNames.add(link);
            }

            if (compatibleNames.size() > 0) {
                EmbedBuilder b = EmbedUtil.createSuccessEmbed();
                b.setDescription("Folgende Verknüpfungen wurden entfernt:");
                for (LinkCodec compatibleName : compatibleNames) {
                    directory.removeLink(compatibleName);
                    b.addField(compatibleName.getName(), compatibleName.getLink(), false);
                }
                event.getHook().editOriginalEmbeds(b.build()).queue();
            } else {
                EmbedBuilder b = EmbedUtil.createErrorEmbed();
                b.setDescription("Folgende Verknüpfungen konnten nicht gefunden und entfernt werden:");
                for (String incompatibleName : incompatibleNames) {
                    b.addField(incompatibleName, "", false);
                }
                event.getHook().editOriginalEmbeds(b.build()).queue();
                return;
            }

            if (incompatibleNames.size() > 0) {
                EmbedBuilder b = EmbedUtil.createErrorEmbed();
                b.setDescription("Folgende Verknüpfungen konnten nicht gefunden und entfernt werden:");
                for (String incompatibleName : incompatibleNames) {
                    b.addField(incompatibleName, "", false);
                }
                event.getHook().editOriginalEmbeds(b.build()).queue();
            }
        }

    }

    public void handleAddDir(SlashCommandEvent event) {
        OptionMapping dirNameMapping = event.getOption("dir-name");
        OptionMapping publicMapping = event.getOption("public");
        if (dirNameMapping == null || publicMapping == null) {
            EmbedBuilder b = EmbedUtil.createErrorEmbed();
            b.setDescription("Übergebe den Namen und die Sichtbarkeit (public/private) des zu erstellenden Ordners");
            event.getHook().editOriginalEmbeds(b.build()).queue();
            return;
        }

        String dirName = dirNameMapping.getAsString();
        boolean shared = publicMapping.getAsBoolean();

        List<LinkCodec> compatibleNames = new ArrayList<>();
        List<String> incompatibleNames = new ArrayList<>();

        DirectoryCodec directoryCodec = DirectoryCodec.addDirectory(event.getUser(), dirName, compatibleNames);
        if (directoryCodec == null) {
            EmbedBuilder b = EmbedUtil.createErrorEmbed();
            b.setDescription("Eine Sammlung mit diesem Namen existiert bereits");
            event.getHook().editOriginalEmbeds(b.build()).queue();
            return;
        }

        directoryCodec.setShared(shared);

        EmbedBuilder b = EmbedUtil.createSuccessEmbed();
        b.setDescription("Deine Sammlung wurde erfolgreich erstellt. Füge Verknüpfungen mit dem editDirectory Command hinzu");
        event.getHook().editOriginalEmbeds(b.build()).queue();
    }

    public void handleGetDir(SlashCommandEvent event) {
        String dirName = Objects.requireNonNull(event.getOption("dir-name")).getAsString();
        boolean isEmpty = true;
        for (DirectoryCodec directory : DirectoryCodec.getDirectories(event.getUser(), true)) {
            if (directory.getName().equalsIgnoreCase(dirName)) {
                isEmpty = false;
                EmbedBuilder b = EmbedUtil.createDirectoryEmbed();
                b.setDescription("Sichtbarkeit public?: " + directory.isShared() + "\nListe aller Verknüpfungen:");
                for (LinkCodec linkCodec : directory.getLinks()) {
                    b.addField(linkCodec.getName(), linkCodec.getLink(), false);
                }
                b.setAuthor(event.getUser().getName());
                event.getHook().editOriginalEmbeds(b.build()).queue();
            }
        }

        if (isEmpty) {
            EmbedBuilder b = EmbedUtil.createErrorEmbed();
            b.setDescription("Keine Sammlung für diesen Namen gefunden");
            event.getHook().editOriginalEmbeds(b.build()).queue();
        }
    }
}
