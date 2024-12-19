package net.qilla.destructible.mining.player.data;

public final class Durability {
    private final float total;
    private float current;

    public Durability(float total) {
        this.total = total;
        this.current = total;
    }

    public Durability(float total, float current) {
        this.total = total;
        this.current = current;
    }

    public float damage(float amount) {
        return this.current -= amount;
    }

    public float repair(float amount) {
        return this.current += amount;
    }

    public boolean isBroken() {
        return this.current <= 0;
    }

    public float getTotal() {
        return this.total;
    }

    public float getCurrent() {
        return this.current;
    }
}