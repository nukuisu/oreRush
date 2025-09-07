package sample.Command;

import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.jetbrains.annotations.NotNull;
import sample.example.oreRush.OreRush;

public class CommandOreRush implements CommandExecutor {

  @Override
  public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
      @NotNull String label, @NotNull String[] args) {
    if (sender instanceof Player player) {

      //oreRushと打つとプレイヤーの装備を整える
      player.setHealth(20);
      player.setFoodLevel(20);

      PlayerInventory inventory = player.getInventory();
      inventory.setHelmet(new ItemStack(Material.IRON_HELMET));
      inventory.setChestplate(new ItemStack(Material.IRON_CHESTPLATE));
      inventory.setLeggings(new ItemStack(Material.IRON_LEGGINGS));
      inventory.setBoots(new ItemStack(Material.IRON_BOOTS));
      inventory.setItemInMainHand(new ItemStack(Material.DIAMOND_PICKAXE));

      player.sendTitle(
          "ゲームスタート","鉱石をたくさん掘れ",0,20,0);




    }
    return false;
  }
}

