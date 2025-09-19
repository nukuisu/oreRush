package sample.GameSession;

import java.util.EnumMap;
import java.util.Map;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

public class GameSession {
  public static final int GAME_TIME = 30;
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

  public GameSession(int id, Player player) {
    this.id = id;
    this.player = player;
    this.active = true;
    this.score = 0;
    this.remainingTime = GAME_TIME;

    orePoints.put(Material.COPPER_ORE,10);
    orePoints.put(Material.GOLD_ORE,20);
    orePoints.put(Material.DIAMOND_ORE,50);
  }

  /**
   * プレイヤーの装備を初期化と採掘場所へワープ
   */
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

  /**
   * 制限時間をカウントする
   * true = 終了した
   * false = まだ続行中
   */
  public boolean tick(){

    if (!active) return true;

    this.remainingTime--;

    if (this.remainingTime == 10) {
      this.player.sendMessage("残り10秒です");
    }
    if (this.remainingTime <= 0) {
      this.active = false;
      return true;
    }
    return false;
  }


  public int getPoint(Material type) {
    return orePoints.getOrDefault(type, 0);
  }

  /**
   *プレイヤーが鉱石を壊すと点数が加算される
   * 銅（10点）、金（20点）、ダイヤモンド（50点）
   */
  public void addScore(Block block){
    Material type = block.getType();

    int point = getPoint(type);

    if (point > 0) {
      this.score += point;
      this.player.sendMessage("採掘完了！現在のスコア: " + this.score + "点");
    }
  }

}

