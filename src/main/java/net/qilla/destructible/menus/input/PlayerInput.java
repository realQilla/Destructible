package net.qilla.destructible.menus.input;

import java.util.List;
import java.util.function.Consumer;

public interface PlayerInput {
    void init(List<String> signText, Consumer<String> onComplete);
}
