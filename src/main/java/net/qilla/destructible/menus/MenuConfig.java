package net.qilla.destructible.menus;

import net.kyori.adventure.text.Component;
import net.qilla.destructible.menus.slot.Socket;

public interface MenuConfig {

    Component tile();
    MenuSize menuSize();
    Socket menuSocket();
    Socket returnSocket();
}
