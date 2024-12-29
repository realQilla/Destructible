package net.qilla.destructible.mining.block;

public class DNode extends DBlock {

    private final int minOre;
    private final int maxOre;

    public DNode(DBlock.Properties dBlockBlockProperties, DNode.Properties properties) {
        super(dBlockBlockProperties);
        this.minOre = properties.minOre;
        this.maxOre = properties.maxOre;
    }

    public int getMinOre() {
        return this.minOre;
    }

    public int getMaxOre() {
        return this.maxOre;
    }

    public static class Properties {
        public int minOre;
        public int maxOre;

        public static DNode.Properties of() {
            return new DNode.Properties();
        }

        public DNode.Properties minOre(int minOre) {
            this.minOre = minOre;
            return this;
        }

        public DNode.Properties maxOre(int maxOre) {
            this.maxOre = maxOre;
            return this;
        }
    }
}
