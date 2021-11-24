package tech.ypsilon.bbbot.config;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Data
@Builder
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
public class CommandsSubconfig {

    @JsonProperty("bdaychannel")
    private Long birthdayChannel;

    @JsonProperty("bday")
    private BDaySubconfig birthday;

    @Data
    @Builder
    @EqualsAndHashCode
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BDaySubconfig {
        private boolean notifyNoBirthdays;
    }

}
