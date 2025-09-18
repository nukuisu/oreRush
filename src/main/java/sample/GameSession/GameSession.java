package sample.GameSession;

import java.util.EnumMap;
import java.util.Map;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

public class GameSession {
  private static final int MAX_HEALTH = 20;
  private static final int MAX_FOOD_LEVEL = 20;
  private static final int START_X = 0;
  private static final int START_Y = 110;
  private static final int START_Z = 0;
  private final  Map<Material,Integer> orePoints = new EnumMap<>(Material.class);
  public final int id;
  public boolean active;
  public int score;
  public int remainingTime;
  public Player player;

  public GameSession(int id, Player player, int gameTime) {
    this.id = id;
    this.player = player;
    this.active = true;
    this.score = 0;
    this.remainingTime = gameTime;

    orePoints.put(Material.COPPER_ORE,10);
    orePoints.put(Material.GOLD_ORE,20);
    orePoints.put(Material.DIAMOND_ORE,50);
  }

  public void preparePlayer() {
    this.player.setHealth(MAX_HEALTH);
    this.player.setFoodLevel(MAX_FOOD_LEVEL);

    PlayerInventory inventory = this.player.getInventory();
    inventory.setHelmet(new ItemStack(Material.IRON_HELMET));
    inventory.setChestplate(new ItemStack(Material.IRON_CHESTPLATE));
    inventory.setLeggings(new ItemStack(Material.IRON_LEGGINGS));
    inventory.setBoots(new ItemStack(Material.IRON_BOOTS));
    inventory.setItemInMainHand(new ItemStack(Material.IRON_PICKAXE));

    this.player.sendTitle(
        "ゲームスタート", "鉱石をたくさん掘れ", 0, 30, 0);

    Location location = new Location(this.player.getWorld(), START_X, START_Y, START_Z);
    this.player.teleport(location);
  }

  public int getPoint(Material type) {
    return orePoints.getOrDefault(type, 0);
  }

}

