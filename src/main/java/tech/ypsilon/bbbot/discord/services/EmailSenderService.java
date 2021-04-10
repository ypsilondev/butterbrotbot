package tech.ypsilon.bbbot.discord.services;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.MimeMessage;
import java.util.Properties;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public class EmailSenderService {

    private static final EmailSenderService INSTANCE = new EmailSenderService();

    public static void start() {
        INSTANCE.run();
    }

    private final BlockingQueue<SendAction> messageQueue;
    private final Session mailSession;

    private final String username = "notificationdiscord@gmail.com";
    private final String password = "aLEr6KBHEdRz5Nr";

    private EmailSenderService() {
        messageQueue = new ArrayBlockingQueue<>(1);
        Properties prop = new Properties();
        prop.put("mail.smtp.host", "smtp.gmail.com");
        prop.put("mail.smtp.port", "465");
        prop.put("mail.smtp.auth", "true");
        prop.put("mail.smtp.socketFactory.port", "465");
        prop.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
        mailSession = Session.getInstance(prop, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(username, password);
            }
        });
    }

    private void run() {
        new Thread(() -> {
            Logger logger = LoggerFactory.getLogger(EmailSenderService.class);
            synchronized (messageQueue) {
                while (true) {
                    try {
                        messageQueue.wait();
                        while(!messageQueue.isEmpty()) {
                            SendAction action = messageQueue.poll(10, TimeUnit.SECONDS);
                            if (action != null) {
                                sendEmail(action);
                            } else {
                                logger.error("MessageQueue contained null object");
                            }
                        }
                    } catch (InterruptedException exception) {
                        exception.printStackTrace();
                    }
                }
            }
        }).start();
    }

    private void sendEmail(SendAction action) {
        try {
            Message message = new MimeMessage(mailSession);
            message.setRecipients(Message.RecipientType.TO, action.getMessage().getAllRecipients());
            message.setSubject(action.getMessage().getSubject());
            Transport.send(message);
        } catch (MessagingException exception) {
            exception.printStackTrace();
            action.getFuture().obtrudeException(exception);
        }
    }

    private CompletableFuture<Void> queue(Message message) {
        CompletableFuture<Void> future = new CompletableFuture<>();
        messageQueue.add(new SendAction(message, future));
        messageQueue.notifyAll();
        return future;
    }

    @Data
    @AllArgsConstructor
    private static class SendAction {
        private final Message message;
        private final CompletableFuture<Void> future;
    }
}
