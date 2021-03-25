package tech.ypsilon.bbbot.console.commands;

import tech.ypsilon.bbbot.ButterBrot;
import tech.ypsilon.bbbot.console.ConsoleCommand;

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
        ButterBrot.LOGGER.info("This command is not longer supported; the only reason it existed is gone since a long time.");
        /*SimpleDateFormat format = new SimpleDateFormat("dd.MM.yyyy");
        BirthdayMongoDBWrapper.oldGetBirthdayEntrys().forEach((s, s2) -> {
            try {
                Long aLong = Long.valueOf(s.substring(21, 39));
                Date parse = format.parse(s2);
                BirthdayCodec birthdayCodec = BirthdayCodec.newBirthday(aLong, parse);
                System.out.println(aLong + " - " + parse + " - " + s2);
            } catch (ParseException e) {
                e.printStackTrace();
            }
        });*/
    }
}
