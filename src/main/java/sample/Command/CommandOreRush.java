package sample.Command;

import java.util.EnumMap;
import java.util.Map;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;
import sample.example.oreRush.OreRush;


public class CommandOreRush implements CommandExecutor, Listener {

  public static final int GAME_TIME = 30;

  private final OreRush oreRush;

  private final Map<Material,Integer> orePoints = new EnumMap<>(Material.class);
  private Player player;
  private int score;
  private boolean gameActive = false; //

  public CommandOreRush(OreRush oreRush) {
    this.oreRush = oreRush;
    orePoints.put(Material.COPPER_ORE,10);
    orePoints.put(Material.GOLD_ORE,20);
    orePoints.put(Material.DIAMOND_ORE,50);
  }


  @Override
  public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
      @NotNull String label, @NotNull String[] args) {



    if (sender instanceof Player Senderplayer) {

      this.player = Senderplayer;
      this.gameActive = true;
      this.score = 0;

      //oreRushと打つとプレイヤーの装備を整え、採掘場所へワープする(2h)
      Senderplayer.setHealth(20);
      Senderplayer.setFoodLevel(20);

      PlayerInventory inventory = Senderplayer.getInventory();
      inventory.setHelmet(new ItemStack(Material.IRON_HELMET));
      inventory.setChestplate(new ItemStack(Material.IRON_CHESTPLATE));
      inventory.setLeggings(new ItemStack(Material.IRON_LEGGINGS));
      inventory.setBoots(new ItemStack(Material.IRON_BOOTS));
      inventory.setItemInMainHand(new ItemStack(Material.IRON_PICKAXE));

      Senderplayer.sendTitle(
          "ゲームスタート", "鉱石をたくさん掘れ", 0, 30, 0);

      Location location = new Location(Senderplayer.getWorld(), 0, 110, 0);

      Senderplayer.teleport(location);

      //制限時間を10秒に設定し、ゲーム終了時にメッセージを出す(3h)
      new BukkitRunnable() {

        int remainingTime = GAME_TIME;

        public void run() {

          if (remainingTime<= 0) {

            cancel();
            gameActive = false;

            Player p = CommandOreRush.this.player;
            if(p !=null){
              p.sendTitle("ゲームが終了しました！",
                  "合計"+score+"点！",
                  0, 60, 0);
            }

            CommandOreRush.this.player = null;
            CommandOreRush.this.score = 0;

          }
          remainingTime--;

        }

      }.runTaskTimer(oreRush, 0L, 20L);


    }
    return true;


  }
  //鉱石ごとに点数をカウントする(7h)
  @EventHandler
  public void onBlockBreak(BlockBreakEvent b) {

    if (!gameActive || player == null) return;

    if (!b.getPlayer().getUniqueId().equals(player.getUniqueId())) return;

    Block block = b.getBlock();
    Material type = block.getType();


    int point = orePoints.getOrDefault(type,0);

    if (point > 0) {
      score += point;
      player.sendMessage("採掘完了！現在のスコア: " + score + "点");
    }
  }

}