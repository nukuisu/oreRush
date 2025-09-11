package sample.Command;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;
import sample.example.oreRush.OreRush;


public class CommandOreRush implements CommandExecutor {

  public static final int GAME_TIME = 10;

  private final OreRush oreRush;

  public CommandOreRush(OreRush oreRush) {
    this.oreRush = oreRush;
  }


  @Override
  public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
      @NotNull String label, @NotNull String[] args) {
    if (sender instanceof Player player) {

      //oreRushと打つとプレイヤーの装備を整え、採掘場所へワープする(2h)
      player.setHealth(20);
      player.setFoodLevel(20);

      PlayerInventory inventory = player.getInventory();
      inventory.setHelmet(new ItemStack(Material.IRON_HELMET));
      inventory.setChestplate(new ItemStack(Material.IRON_CHESTPLATE));
      inventory.setLeggings(new ItemStack(Material.IRON_LEGGINGS));
      inventory.setBoots(new ItemStack(Material.IRON_BOOTS));
      inventory.setItemInMainHand(new ItemStack(Material.IRON_PICKAXE));

      player.sendTitle(
          "ゲームスタート", "鉱石をたくさん掘れ", 0, 30, 0);

      Location location = new Location(player.getWorld(), 0, 110, 0);

      player.teleport(location);

      //制限時間を10秒に設定し、ゲーム終了時にメッセージを出す(3h)
      new BukkitRunnable() {

        int remainingTime = GAME_TIME;

        public void run() {

          if (remainingTime<= 0) {

            cancel();

            player.sendTitle(
                "ゲームが終了しました！",
                "合計〇〇点！",
                0, 60, 0
            );
          }
          remainingTime--;
        }

      }.runTaskTimer(oreRush, 0L, 20L);


    }
    return false;



  }
}