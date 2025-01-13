package net.qilla.destructible.menus;

import net.qilla.destructible.menus.slot.Socket;

import java.util.List;

public interface ModularConfig extends MenuConfig {

    List<Integer> modularIndexes();
    Socket nextSocket();
    Socket previousSocket();
}