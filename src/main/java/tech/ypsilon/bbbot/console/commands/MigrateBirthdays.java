package tech.ypsilon.bbbot.console.commands;

import tech.ypsilon.bbbot.console.ConsoleCommand;
import tech.ypsilon.bbbot.database.codecs.BirthdayCodec;
import tech.ypsilon.bbbot.database.wrapper.BirthdayMongoDBWrapper;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MigrateBirthdays extends ConsoleCommand {
    @Override
    public String[] getAlias() {
        return new String[]{"migrateBirthdays"};
    }

    @Override
    public String getDescription() {
        return null;
    }

    @Override
    public void onExecute(String[] args) {
        SimpleDateFormat format = new SimpleDateFormat("dd.MM.yyyy");
        BirthdayMongoDBWrapper.oldGetBirthdayEntrys().forEach((s, s2) -> {
            try {
                Long aLong = Long.valueOf(s.substring(21, 39));
                Date parse = format.parse(s2);
                BirthdayCodec birthdayCodec = BirthdayCodec.newBirthday(aLong, parse);
                System.out.println(aLong + " - " + parse + " - " + s2);
            } catch (ParseException e) {
                e.printStackTrace();
            }
        });
    }
}
