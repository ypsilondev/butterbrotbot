package tech.ypsilon.bbbot.stats;

import io.prometheus.client.CollectorRegistry;
import io.prometheus.client.Counter;
import io.prometheus.client.Gauge;
import io.prometheus.client.exporter.HTTPServer;
import tech.ypsilon.bbbot.ButterBrot;
import tech.ypsilon.bbbot.util.GenericController;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class StatsController extends GenericController {

    private static final int PORT = 9090;

    private static StatsController instance;

    public static StatsController getInstance() {
        return instance;
    }

    public StatsController(ButterBrot parent) {
        super(parent);

        instance = this;
        Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(new StatsPoll(), 1, 30, TimeUnit.MINUTES);

        try {
            HTTPServer server = new HTTPServer(new InetSocketAddress(PORT), CollectorRegistry.defaultRegistry, false);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Gauge getGauge(String name, String help, String... labels) {
        Gauge gauge = new Gauge.Builder().name(name).help(help).labelNames(labels).create();
        gauge.register();

        return gauge;
    }

    public Counter getCounter(String name, String help, String... labels) {
        Counter counter = new Counter.Builder().name(name).help(help).labelNames(labels).create();
        counter.register();

        return counter;
    }

}