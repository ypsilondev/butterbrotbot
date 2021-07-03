package tech.ypsilon.bbbot.stats;

import io.prometheus.client.CollectorRegistry;
import io.prometheus.client.Counter;
import io.prometheus.client.Gauge;
import io.prometheus.client.exporter.HTTPServer;

import java.io.IOException;
import java.net.InetSocketAddress;

public class StatsManager {

    private static final int PORT = 9090;

    private static StatsManager instance;
    private final CollectorRegistry registry;

    public static StatsManager getInstance() {
        if (instance == null) {
            instance = new StatsManager();
        }
        return instance;
    }

    private StatsManager() {
        instance = this;

        registry = new CollectorRegistry();
        try {
            HTTPServer server = new HTTPServer(new InetSocketAddress(PORT), registry, false);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Gauge getGauge(String name, String help, String... labels) {
        Gauge gauge = new Gauge.Builder().name(name).help(help).labelNames(labels).create();
        gauge.register(registry);

        return gauge;
    }

    public Counter getCounter(String name, String help, String... labels) {
        Counter counter = new Counter.Builder().name(name).help(help).labelNames(labels).create();
        counter.register(registry);

        return counter;
    }

}
