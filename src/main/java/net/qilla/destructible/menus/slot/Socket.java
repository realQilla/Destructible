package net.qilla.destructible.menus.slot;

public class Socket {

    private final int index;
    private final Slot slot;

    public Socket(int index, Slot slot) {
        this.index = index;
        this.slot = slot;
    }

    public static Socket of(int index, Slot slot) {
        return new Socket(index, slot);
    }

    public int index() {
        return index;
    }

    public Slot slot() {
        return slot;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Socket socket = (Socket) o;
        return index == socket.index && slot.equals(socket.slot);
    }

    @Override
    public int hashCode() {
        int result = Integer.hashCode(index);
        result = 31 * result + slot.hashCode();
        return result;
    }
}
