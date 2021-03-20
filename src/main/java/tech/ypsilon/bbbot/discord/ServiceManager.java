package tech.ypsilon.bbbot.discord;

import net.dv8tion.jda.api.JDA;
import tech.ypsilon.bbbot.discord.services.BirthdayNotifierService;
import tech.ypsilon.bbbot.discord.services.GuildNotifierService;

import java.util.ArrayList;
import java.util.List;

public class ServiceManager {

    private final List<GuildNotifierService> notifierServices;
    public static ServiceManager instance;

    public ServiceManager() {
        instance = this;
        this.notifierServices = new ArrayList<>();
    }

    private void registerAllServices(JDA jda){
        //this.registerService(new BirthdayNotifierService(jda));
    }

    public void initialize() {
        new Thread(() -> {
            JDA jda = DiscordController.getJDA();
            try {
                // JDA might not be set by DiscordController due to async...
                while ((jda = DiscordController.getJDA()) == null) {
                    Thread.sleep(100);
                }
                // Waiting for JDA to really finish startup-crap...
                jda.awaitStatus(JDA.Status.CONNECTED);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            this.registerAllServices(jda);
        }).start();
    }

    public GuildNotifierService findNotifierService(Class<? extends GuildNotifierService> clazz) {
        for (GuildNotifierService service : this.notifierServices) {
            if (service.getClass().equals(clazz)) {
                return service;
            }
        }
        return null;
    }

    private void registerService(GuildNotifierService service) {
        this.notifierServices.add(service);
        service.startService();
    }

}
