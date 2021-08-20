package tech.ypsilon.bbbot.discord.command;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.SelectionMenuEvent;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.selections.SelectOption;
import net.dv8tion.jda.api.interactions.components.selections.SelectionMenu;
import org.jetbrains.annotations.Nullable;
import tech.ypsilon.bbbot.discord.SlashCommandManager;
import tech.ypsilon.bbbot.util.EmbedUtil;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;

public class HelpSlashCommand extends SlashCommand {

    @Override
    public CommandData commandData() {
        return new CommandData("help", "Erklärt alle Befehle.");
    }

    @Override
    public void execute(SlashCommandEvent event) {
        event.deferReply(true).queue();

        Map<String, SlashCommand> commands = new TreeMap<>(SlashCommandManager.getInstance().getCommandMap());

        SelectionMenu.Builder builder = SelectionMenu.create(createSelectMenuId("help"))
                .setPlaceholder("Befehl")
                .setRequiredRange(1, 10);

        commands.forEach((lbl, command) -> {
            if (command.getHelpDescription() != null) {
                builder.addOption("/" + command.getName(), command.getName());
            }
        });

        event.getHook().editOriginal("Bitte wähle einen Befehl aus:").queue();
        event.getHook().editOriginalComponents(ActionRow.of(builder.build())).queue();

    }

    @Override
    public void handleSelectionMenu(SelectionMenuEvent event, String data) {
        if (event.getSelectedOptions() == null) throw new CommandFailedException("Es ist ein Fehler aufgetreten");

        List<MessageEmbed> embeds = new ArrayList<>();

        Map<String, SlashCommand> commands = SlashCommandManager.getInstance().getCommandMap();
        for (SelectOption option : Objects.requireNonNull(event.getSelectedOptions())) {
            SlashCommand command = commands.get(option.getValue());
            if (command.getHelpDescription() == null) {
                continue;
            }
            EmbedBuilder builder = new EmbedBuilder();
            builder.setColor(Color.decode("#97DBA2"));
            builder.setTitle(option.getLabel()).setDescription(command.getHelpDescription());
            embeds.add(builder.build());
        }

        event.editMessage("").setEmbeds(embeds).queue();
    }

    @Override
    public @Nullable String getHelpDescription() {
        return "Listet diese Übersicht über alle Befehle auf.";
    }
}
