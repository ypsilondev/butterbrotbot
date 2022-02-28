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
import tech.ypsilon.bbbot.ButterBrot;
import tech.ypsilon.bbbot.discord.SlashCommandController;

import java.awt.*;
import java.util.List;
import java.util.*;

public class HelpSlashCommand extends SlashCommand {

    public HelpSlashCommand(ButterBrot parent) {
        super(parent);
    }

    @Override
    public CommandData commandData() {
        return new CommandData("help", "Erklärt alle Befehle.");
    }

    @Override
    public void execute(SlashCommandEvent event) {
        event.deferReply(true).queue();

        Map<String, SlashCommand> commands = new TreeMap<>(SlashCommandController.getInstance().getCommandMap());

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

        Map<String, SlashCommand> commands = SlashCommandController.getInstance().getCommandMap();
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
