package tech.ypsilon.bbbot.discord;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.exceptions.ErrorResponseException;
import tech.ypsilon.bbbot.ButterBrot;

import java.util.Arrays;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

public class AliasCommandExecutor {

    public static void execute(GuildMessageReceivedEvent e, String[] args) {
        int deleteCounter = 0;
        try {
            deleteCounter = Integer.parseInt(args[0]);
        } catch (NumberFormatException e1) {
            ButterBrot.LOGGER.warn(String.format("Error while parsing delete-time (%s)", args[0]));
        }

        // Build message-string
        String msg = String.join(" ", Arrays.copyOfRange(args, 1, args.length));

        // Send and store message
        AtomicReference<Message> message = new AtomicReference<>();
        CountDownLatch countDownLatch = new CountDownLatch(1);
        e.getChannel().sendMessage(msg).queue(mess -> {
            message.set(mess);
            countDownLatch.countDown();
        });


        // Delete message after specified time.
        if (deleteCounter > 0) {
            try {
                countDownLatch.await();
            } catch (InterruptedException interruptedException) {
                interruptedException.printStackTrace();
            }
            final int finalDeleteCounter = deleteCounter;
            new Thread(() -> {
                try {
                    Thread.sleep(finalDeleteCounter);
                } catch (InterruptedException interruptedException) {
                    interruptedException.printStackTrace();
                }
                try {
                    message.get().delete().queue();
                } catch (ErrorResponseException e2) {
                    // ignored; only thrown, when message got deleted somewhere else.
                }
            }).start();
        }
    }


}
