package sample.example.oreRush;

import java.util.Objects;
import org.bukkit.Bukkit;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import sample.Command.CommandOreRush;

public final class OreRush extends JavaPlugin implements Listener {

  @Override
  public void onEnable() {
    CommandOreRush commandOreRush =new CommandOreRush(this);
    Bukkit.getPluginManager().registerEvents(commandOreRush, this);
    Objects.requireNonNull(getCommand("oreRush")).setExecutor(commandOreRush);
  }

}


