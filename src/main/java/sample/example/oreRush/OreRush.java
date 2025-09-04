package sample.example.oreRush;

import org.bukkit.plugin.java.JavaPlugin;
import sample.Command.CommandOreRush;

public final class OreRush extends JavaPlugin {

  @Override
  public void onEnable() {
    // Plugin startup logic
    CommandOreRush commandOreRush = new CommandOreRush;


  }

  @Override
  public void onDisable() {
    // Plugin shutdown logic
  }
}
