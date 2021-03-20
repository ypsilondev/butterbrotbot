package tech.ypsilon.bbbot.discord.services;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.TextChannel;
import org.bson.Document;
import tech.ypsilon.bbbot.database.MongoController;
import tech.ypsilon.bbbot.database.MongoSettings;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BirthdayNotifierService extends GuildNotifierService {


    public BirthdayNotifierService(JDA jda) {
        super(getChannel(jda));
    }

    private String t;
    @Override
    public void execute(TextChannel channel) {
        Map<Long, Date> bdays = this.getBirthdays();
        bdays.keySet().forEach(userId -> {
            notifyBday(channel, userId, bdays.get(userId));
        });
    }

    private void notifyBday(TextChannel channel, long userId, Date bday) {
        List<Member> members = channel.getGuild().retrieveMembersByIds(userId).get();
        if (!members.isEmpty()) {
            String userName = members.get(0).getAsMention();

            Date now = new Date(System.currentTimeMillis());
            SimpleDateFormat formatter = new SimpleDateFormat("YYYY");
            int age = Integer.parseInt(formatter.format(now)) - Integer.parseInt(formatter.format(bday));

            channel.sendMessage(userName + " hat heute Geburtstag und wurde " + age + " Jahre alt!\nHerzlichen Glückwunsch!")
                    .queue(message -> {
                        message.addReaction("U+1F381").queue();
                        message.addReaction("U+1F382").queue();
                    });
        }
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

    private boolean hasBirthdayToday(Date bday) {
        Date now = new Date(System.currentTimeMillis());
        SimpleDateFormat formatter = new SimpleDateFormat("dd.MM");
        return formatter.format(now).equals(formatter.format(bday));
    }

    @Override
    public NotifyTime getNotifyTime() {
        return new NotifyTime(8, 0, 0, NotifyTime.DAILY);
    }

    @Override
    public String getServiceName() {
        return "Birthday";
    }

    private static TextChannel getChannel(JDA jda) {
        long channelId = ((Long) MongoSettings.getValue(MongoSettings.TYPE.BirthdayChannel, 756547960229199902L));
        return jda.getTextChannelById(channelId);
    }
}
