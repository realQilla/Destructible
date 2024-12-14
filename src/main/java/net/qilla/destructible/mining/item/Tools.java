package net.qilla.destructible.mining.item;

public class Tools {
    public static final Tool WOODEN_PICKAXE = register("wooden_pickaxe",new Pickaxe("Wooden Pickaxe", Rarity.COMMON, 1, 100, 1, 1));
    public static final Tool WOODEN_AXE = register("wooden_axe",new Axe("Wooden Axe", Rarity.COMMON, 1, 100, 1, 1));

    static {
        System.out.println("Tools class loaded and static fields initialized.");
    }

    private static Tool register(String id, Tool tool) {
        System.out.println("Registering tool: " + id);
        return ItemRegistry.getInstance().registerItem(id, tool);
    }
}