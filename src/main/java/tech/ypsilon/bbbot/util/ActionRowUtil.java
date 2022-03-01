package tech.ypsilon.bbbot.util;

import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.Button;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Slf4j
public class ActionRowUtil {

    /**
     * Fills action rows with buttons, ensuring that valid action rows are created
     *
     * @param buttons all buttons (max 25) to be added
     * @return all action rows which can then be set
     */
    public static @NotNull Collection<ActionRow> fillButtons(@NotNull List<Button> buttons) {
        if (buttons.size() > 25) {
            RuntimeException exception = new IllegalArgumentException();
            log.error("Cannot fit more than 25 buttons inside ActionRow", exception);
            throw exception;
        }

        int buttonsLeft = buttons.size();
        List<ActionRow> actionRows = new ArrayList<>();

        while (buttonsLeft > 0) {
            List<Button> actionRowList = new ArrayList<>();

            for (int i = 0; i < 5; i++) {
                if (buttonsLeft > 0) {
                    actionRowList.add(buttons.get(buttons.size() - buttonsLeft));
                    buttonsLeft--;
                } else break;
            }

            actionRows.add(ActionRow.of(actionRowList));
        }

        // return unmodifiable list
        return List.copyOf(actionRows);
    }

}
