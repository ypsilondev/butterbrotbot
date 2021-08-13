package tech.ypsilon.bbbot.discord.services;

import net.dv8tion.jda.api.entities.TextChannel;
import tech.ypsilon.bbbot.ButterBrot;
import tech.ypsilon.bbbot.settings.SettingsController;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class AliasService extends GuildNotifierService {

    private static final Map<String, String> ALIAS = new HashMap<>();
    private static boolean disabled = false;

    public static String getAlias(String key) {
        return disabled ? null : ALIAS.get(key);
    }

    @Override
    protected void onExecute(TextChannel channel) {
        try {
            URL url = new URL(Objects.requireNonNull(SettingsController.getString("discord.aliases")));
            BufferedReader reader = new BufferedReader(new InputStreamReader(url.openConnection().getInputStream()));

            disabled = true;
            ALIAS.clear();

            String line;
            while ((line = reader.readLine()) != null) {
                String[] data = line.split("->");

                String alias = data[0];
                String command = line.replaceFirst(alias + "->", "");

                ALIAS.put(alias, command);
            }
            disabled = false;
        } catch (Exception e) {
            e.printStackTrace();
        }

        ButterBrot.LOGGER.info(ALIAS.entrySet().toString());
        ButterBrot.LOGGER.info("update alias service");
    }

    @Override
    public NotifyTime getNotifyTime() {
        return new NotifyTime(0, 0, 0, NotifyTime.HOURLY).noInitialDelay();
    }

    @Override
    public String getServiceName() {
        return "Alias";
    }
}
