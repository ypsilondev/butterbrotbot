package tech.ypsilon.bbbot.discord.services;

import net.dv8tion.jda.api.entities.TextChannel;
import tech.ypsilon.bbbot.ButterBrot;
import tech.ypsilon.bbbot.settings.SettingsController;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

public class ToolUpdaterService extends GuildNotifierService {

    public static HashMap<String, String> links;

    private static String requestUrl;

    public ToolUpdaterService() {
        super();
        ToolUpdaterService.links = new LinkedHashMap<>();
        ToolUpdaterService.requestUrl = SettingsController.getString("discord.toolsurl");
    }

    @Override
    protected void onExecute(TextChannel channel) {
        ButterBrot.LOGGER.info(String.format("[%s] updating link-list", this.getServiceName()));
        List<String> lines = Arrays.asList(getUrlContents(ToolUpdaterService.requestUrl).split("\n"));

        HashMap<String, String> newList = new LinkedHashMap<>();

        String title = "";
        StringBuilder links = new StringBuilder();
        for (String line : lines) {
            if (!line.startsWith("http")) {
                if (links.length() > 0) {
                    newList.put(title, links.toString());
                }
                title = String.format("**%s**", line);
                links = new StringBuilder();
            } else {
                links.append(line).append("\n");
            }
        }
        ToolUpdaterService.links = newList;
        ButterBrot.LOGGER.info(String.format("[%s] updated link-list", this.getServiceName()));
    }

    @Override
    public NotifyTime getNotifyTime() {
        return new NotifyTime(8, 0, 0, NotifyTime.HOURLY).noInitialDelay();
    }

    @Override
    public String getServiceName() {
        return "Tool-updater-service";
    }

    private static String getUrlContents(String urlString) {
        StringBuilder content = new StringBuilder();
        try {
            URL url = new URL(urlString);
            URLConnection urlConnection = url.openConnection();
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                content.append(line + "\n");
            }
            bufferedReader.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return content.toString();
    }
}
