package sample.example.oreRush;

import org.bukkit.Bukkit;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import sample.Command.CommandOreRush;

public final class OreRush extends JavaPlugin implements Listener {

  @Override
  public void onEnable() {
    Bukkit.getPluginManager().registerEvents(this,this);
    getCommand("oreRush").setExecutor(new CommandOreRush());

  }

}
