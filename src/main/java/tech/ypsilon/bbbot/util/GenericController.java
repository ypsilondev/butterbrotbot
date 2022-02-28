package tech.ypsilon.bbbot.util;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import tech.ypsilon.bbbot.ButterBrot;

@RequiredArgsConstructor
public abstract class GenericController {
    private final @Getter
    ButterBrot parent;
}
