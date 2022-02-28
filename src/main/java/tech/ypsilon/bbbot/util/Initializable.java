package tech.ypsilon.bbbot.util;

public interface Initializable {

    default void safeInit() {
        try {
            init();
        } catch (Exception exception) {
            throw new ExceptionInInitializerError(exception);
        }
    }

    void init() throws Exception;

}
