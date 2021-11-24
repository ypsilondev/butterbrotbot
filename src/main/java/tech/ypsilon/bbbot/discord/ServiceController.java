package tech.ypsilon.bbbot.discord;

import net.dv8tion.jda.api.JDA;
import tech.ypsilon.bbbot.ButterBrot;
import tech.ypsilon.bbbot.util.GenericController;
import tech.ypsilon.bbbot.discord.services.AliasService;
import tech.ypsilon.bbbot.discord.services.BirthdayNotifierService;
import tech.ypsilon.bbbot.discord.services.GuildNotifierService;
import tech.ypsilon.bbbot.discord.services.ToolUpdaterService;
import tech.ypsilon.bbbot.util.Initializable;

import java.util.ArrayList;
import java.util.List;

public class ServiceController extends GenericController implements Initializable {

    private final List<GuildNotifierService> notifierServices;
    private static ServiceController instance;

    public ServiceController(ButterBrot parent) {
        super(parent);
        instance = this;
        this.notifierServices = new ArrayList<>();
    }

    @Override
    public void init() throws Exception {
        new Thread(() -> {
            JDA jda = DiscordController.getJDAStatic();
            try {
                // JDA might not be set by DiscordController due to async...
                while ((jda = DiscordController.getJDAStatic()) == null) {
                    System.err.println("JDA is null!");
                    // TODO: check if we can remove this (probably not needed anymore)
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

    private void registerAllServices(JDA jda){
        // Register normal services
        this.registerService(new ToolUpdaterService());
        this.registerService(new AliasService());

        if(!ButterBrot.DEBUG_MODE){
            registerDBServices(jda);
        }
    }

    private void registerDBServices(JDA jda){
        // Register services which require the Mongo-DB to be present
        this.registerService(new BirthdayNotifierService(jda));
    }

    public GuildNotifierService findNotifierService(Class<? extends GuildNotifierService> clazz) {
        for (GuildNotifierService service : this.notifierServices) {
            if (service.getClass().equals(clazz)) {
                return service;
            }
        }
        return null;
    }

    public void registerService(GuildNotifierService service) {
        this.notifierServices.add(service);
        service.startService();
    }

    public static ServiceController getInstance() {
        return instance;
    }
}
