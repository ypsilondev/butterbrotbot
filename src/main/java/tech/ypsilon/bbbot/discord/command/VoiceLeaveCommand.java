package tech.ypsilon.bbbot.discord.command;

import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import tech.ypsilon.bbbot.voice.AudioManager;

import java.util.Objects;

public class VoiceLeaveCommand extends Command {
    @Override
    public String[] getAlias() {
        return new String[]{"leave", "quit"};
    }

    @Override
    public void onExecute(GuildMessageReceivedEvent e, String[] args) {
        if (Objects.equals(Objects.requireNonNull(Objects.requireNonNull(e.getMember()).getVoiceState()).getChannel(), e.getGuild().getAudioManager().getConnectedChannel())) {
            e.getGuild().getAudioManager().closeAudioConnection();
        }
    }

    @Override
    public String getDescription() {
        return null;
    }
}
