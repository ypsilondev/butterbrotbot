package tech.ypsilon.bbbot.discord.services;

import club.minnced.discord.webhook.WebhookClient;
import club.minnced.discord.webhook.send.WebhookMessageBuilder;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.Webhook;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.ypsilon.bbbot.ButterBrot;
import tech.ypsilon.bbbot.database.MongoController;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BirthdayNotifierService extends GuildNotifierService {

    private static Logger logger = LoggerFactory.getLogger(BirthdayNotifierService.class);

    public BirthdayNotifierService(ButterBrot parent) {
        super(parent, getBirthdayChannel(parent));
    }

    @Override
    public void onExecute(TextChannel channel) {
        logger.info("Birthday-onExecute invoked");
        Map<Long, Date> bdays = this.getBirthdays();
        logger.info("Starting iteration");
        bdays.keySet().forEach(userId -> {
            logger.info(String.format("Iteration over bday-key-set (%d)", userId));
            notifyBirthdays(channel, userId, bdays.get(userId));
        });
        logger.info("Iteration ended");
    }

    private void notifyBirthdays(TextChannel channel, long userId, Date birthdayDate) {
        List<Member> members = channel.getGuild().retrieveMembersByIds(userId).get();
        if (!members.isEmpty()) {
            logger.info(String.format("Notifiziere einen Geburtstag (%d)", userId));
            String userName = members.get(0).getAsMention();

            Date now = new Date(System.currentTimeMillis());
            SimpleDateFormat formatter = new SimpleDateFormat("YYYY"); // TODO: use lowercase y (uppercase Y is for week year!)
            int age = Integer.parseInt(formatter.format(now)) - Integer.parseInt(formatter.format(birthdayDate));

            WebhookClient client = this.getBirthdayWebhook(channel);

            WebhookMessageBuilder builder = new WebhookMessageBuilder();
            builder.setUsername("BirthdayBot")
                    .setAvatarUrl("")
                    .setContent(userName + " hat heute Geburtstag und wurde " + age + " Jahre alt!\nHerzlichen Glückwunsch!");

            client.send(builder.build());

            /*channel.sendMessage(userName + " hat heute Geburtstag und wurde " + age + " Jahre alt!\nHerzlichen Glückwunsch!")
                    .queue(message -> {
                        logger.info("Adding reactions");
                        message.addReaction("U+1F381").queue();
                        message.addReaction("U+1F382").queue();
                    });*/
        }
    }

    private WebhookClient getBirthdayWebhook(TextChannel channel) {
        List<Webhook> channelWebhooks = channel.retrieveWebhooks().complete();
        Webhook webhook;

        if (channelWebhooks.size() > 0) {
            webhook = channelWebhooks.stream().findAny().get();
        } else {
            webhook = channel.createWebhook("BirthdayBot").complete();
        }

        if (webhook.getToken() != null) {
            return WebhookClient.withId(webhook.getIdLong(), webhook.getToken());
        } else {
            ButterBrot.LOGGER.warn("Webhook could not be created!");
        }

        return null;
    }

    private Map<Long, Date> getBirthdays() {
        MongoCollection<Document> collection = MongoController.getInstance().getCollection("Birthdays");
        MongoCursor<Document> cursor = collection.find().cursor();
        Document usr;
        Map<Long, Date> output = new HashMap<>();

        while (cursor.hasNext()) {
            usr = cursor.next();
            if (hasBirthdayToday((Date) usr.get("birthday"))) {
                output.put((long) usr.get("userId"), (Date) usr.get("birthday"));
            }
        }
        return output;
    }

    private boolean hasBirthdayToday(Date birthdayDate) {
        Date now = new Date(System.currentTimeMillis());
        SimpleDateFormat formatter = new SimpleDateFormat("dd.MM");
        return formatter.format(now).equals(formatter.format(birthdayDate));
    }

    @Override
    public NotifyTime getNotifyTime() {
        return new NotifyTime(8, 0, 0, NotifyTime.DAILY);
    }

    @Override
    public String getServiceName() {
        return "Birthday";
    }

    private static TextChannel getBirthdayChannel(ButterBrot parent) {
        return parent.getDiscordController().getJda().getTextChannelById(
                parent.getConfig().getCommands().getBirthdayChannel()
        );
    }
}
