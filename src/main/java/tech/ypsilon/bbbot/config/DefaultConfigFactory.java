package tech.ypsilon.bbbot.config;

public abstract class DefaultConfigFactory {

    /**
     * Used to create default configs, subclasses need to reimplement this!
     * @return default config instance
     */
    public static DefaultConfigFactory createDefault() {
        throw new IllegalStateException("A subclass of " + DefaultConfigFactory.class.getName()
                + " did not implement the static method createDefault()");
    }
}
