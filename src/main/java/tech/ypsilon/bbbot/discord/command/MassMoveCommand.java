package tech.ypsilon.bbbot.discord.command;

import net.dv8tion.jda.api.entities.Category;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.VoiceChannel;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import tech.ypsilon.bbbot.ButterBrot;
import tech.ypsilon.bbbot.util.DiscordUtil;
import tech.ypsilon.bbbot.util.EmbedUtil;

import java.util.*;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutionException;


public class MassMoveCommand extends SlashCommand {

    public MassMoveCommand(ButterBrot parent) {
        super(parent);
    }

    @Override
    public CommandData commandData() {
        return new CommandData("massmove", "Moves multiple people at once").addSubcommands(
                new SubcommandData("direct", "Moves people from one channel into another").addOptions(
                        new OptionData(OptionType.CHANNEL, "from", "Source Channel", true),
                        new OptionData(OptionType.CHANNEL, "to", "Destination Channel", true)
                ),
                new SubcommandData("distribute", "Moves people from one channel").addOptions(
                        new OptionData(OptionType.CHANNEL, "from", "Source Channel", true),
                        new OptionData(OptionType.CHANNEL, "to", "Destination Category", true),
                        new OptionData(OptionType.INTEGER, "limit", "Fill channels with a " +
                                "specified amount of clients, rather than distributing them equally", false)
                ),
                new SubcommandData("gather", "Moves all people into one channel (Use with caution!)").addOptions(
                        new OptionData(OptionType.CHANNEL, "to", "Destination Channel", true)
                )
        );
    }

    @Override
    public void execute(SlashCommandEvent event) {
        if (!DiscordUtil.isAdmin(event.getMember())) {
            event.reply("").addEmbeds(EmbedUtil.createNoPermEmbed().build()).queue();
        }

        if (event.getSubcommandName() == null) {
            event.reply("Invalid usage, please try again!").queue();
            return;
        }

        try {
            switch (event.getSubcommandName()) {
                case "direct":
                    direct(event);
                    return;
                case "distribute":
                    distribute(event);
                    return;
                case "gather":
                    gather(event);
                    return;
                default:
                    event.reply("Invalid usage, please try again!").queue();
            }
        } catch (ClassCastException exception) {
            event.reply("Internal type mismatch. Please check your inputs and try again!").queue();
        }
    }

    private void direct(SlashCommandEvent event) {
        VoiceChannel from = (VoiceChannel) Objects.requireNonNull(event.getOption("from")).getAsGuildChannel();
        VoiceChannel to = (VoiceChannel) Objects.requireNonNull(event.getOption("to")).getAsGuildChannel();

        InteractionHook interactionHook = event.deferReply(true).complete();

        Guild guild = event.getGuild();

        if (guild == null) throw new CommandFailedException();

        List<CompletableFuture<?>> futures = new ArrayList<>();

        for (Member member : from.getMembers()) {
            futures.add(guild.moveVoiceMember(member, guild.getVoiceChannelById(to.getId())).submit());
        }

        try {
            // wait for completion
            for (CompletableFuture<?> future : futures) future.get();
        } catch (CancellationException | CompletionException | ExecutionException | InterruptedException exception) {
            throw new CommandFailedException("Konnte nicht alle Mitglieder bewegen!");
        }

        interactionHook.sendMessage("Mitglieder erfolgreich bewegt!").queue();
    }

    private void distribute(SlashCommandEvent event) {
        VoiceChannel from = (VoiceChannel) Objects.requireNonNull(event.getOption("from")).getAsGuildChannel();
        Category to = (Category) Objects.requireNonNull(event.getOption("to")).getAsGuildChannel();
        OptionMapping limitOption = event.getOption("limit");

        InteractionHook interactionHook = event.deferReply(true).complete();

        List<CompletableFuture<?>> scheduler = new ArrayList<>();

        if (limitOption != null && limitOption.getAsLong() > 0) {
            if (from.getMembers().size() > to.getVoiceChannels().size() * limitOption.getAsLong()) {
                interactionHook.sendMessage(
                        String.format("Cannot fit %s people in %s channels with a limit of %s per channel (minimum needed: %s)",
                                from.getMembers().size(),
                                to.getVoiceChannels().size(),
                                limitOption.getAsLong(),
                                (int) Math.ceil((float) from.getMembers().size() / (float) to.getVoiceChannels().size())
                        )
                ).queue();
                return;
            } else {
                distributeLimited(from, to, limitOption.getAsLong(), scheduler, event);
            }
        } else {
            distributeEqually(from, to, scheduler, event);
        }

        try {
            for (CompletableFuture<?> future : scheduler) future.join();
        } catch (CancellationException | CompletionException exception) {
            throw new CommandFailedException("Konnte nicht alle Mitglieder bewegen!");
        }

        interactionHook.sendMessage("Mitglieder erfolgreich bewegt!").queue();
    }

    private void distributeEqually(VoiceChannel from, Category to, List<CompletableFuture<?>> scheduler, SlashCommandEvent event) {
        List<VoiceChannel> voiceChannels = to.getVoiceChannels();
        int channelCount = voiceChannels.size();
        int currentChannel = 0;

        Guild guild = event.getGuild();

        if (guild == null) throw new CommandFailedException();

        for (Member member : from.getMembers()) {
            currentChannel++;
            if (currentChannel >= channelCount) currentChannel = 0;

            scheduler.add(
                    guild.moveVoiceMember(
                            member,
                            voiceChannels.get(currentChannel)
                    ).submit()
            );
        }
    }

    private void distributeLimited(VoiceChannel from, Category to, long limit, List<CompletableFuture<?>> scheduler, SlashCommandEvent event) {
        Iterator<VoiceChannel> voiceChannelIterator = to.getVoiceChannels().iterator();
        VoiceChannel currentChannel = voiceChannelIterator.next();
        int counter = -1;

        Guild guild = event.getGuild();

        if (guild == null) throw new CommandFailedException();

        for (Member member : from.getMembers()) {
            counter++;
            if (counter >= limit) {
                counter = 0;
                currentChannel = voiceChannelIterator.next();
            }

            scheduler.add(
                    guild.moveVoiceMember(
                            member,
                            currentChannel
                    ).submit()
            );
        }
    }

    private void gather(SlashCommandEvent event) {
        VoiceChannel to = (VoiceChannel) Objects.requireNonNull(event.getOption("to")).getAsGuildChannel();
        InteractionHook interactionHook = event.deferReply(true).complete();

        Set<Member> members = new HashSet<>();
        Guild guild = event.getGuild();

        if (guild == null) throw new CommandFailedException();

        for (VoiceChannel voiceChannel : guild.getVoiceChannels()) {
            members.addAll(voiceChannel.getMembers());
        }

        List<CompletableFuture<?>> scheduler = new ArrayList<>();

        for (Member member : members) {
            scheduler.add(
                    guild.moveVoiceMember(member, to).submit()
            );
        }

        try {
            for (CompletableFuture<?> future : scheduler) future.join();
        } catch (CancellationException | CompletionException exception) {
            throw new CommandFailedException("Konnte nicht alle Mitglieder bewegen!");
        }

        interactionHook.sendMessage("Mitglieder erfolgreich bewegt!").queue();
    }
}
