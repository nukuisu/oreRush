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
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;
import sample.example.oreRush.OreRush;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.format.DateTimeFormatter;



public class CommandOreRush implements CommandExecutor, Listener {

  public static final int GAME_TIME = 10;
  private final OreRush oreRush;
  private final Map<Material,Integer> orePoints = new EnumMap<>(Material.class);
  private Player player;
  private int score;
  private boolean gameActive = false; //
  private int remainingTime;
  private BukkitTask timerTask;

  public CommandOreRush(OreRush oreRush) {
    this.oreRush = oreRush;
    orePoints.put(Material.COPPER_ORE,10);
    orePoints.put(Material.GOLD_ORE,20);
    orePoints.put(Material.DIAMOND_ORE,50);
  }


  private void sendScoreList(CommandSender sender) {
    String url  = oreRush.getConfig().getString(
        "database.url",
        "jdbc:mysql://localhost:3306/spigot_server2?useSSL=false&serverTimezone=Asia/Tokyo&characterEncoding=utf8");
    String user = oreRush.getConfig().getString("database.user", "root");
    String pass = oreRush.getConfig().getString("database.password", "1467Ouninn/");

    String sql = """
      SELECT id, player_name, score, registered_at
      FROM player_score
      ORDER BY id DESC
      LIMIT 20
      """;

    DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    try (Connection con = DriverManager.getConnection(url, user, pass);
        PreparedStatement ps = con.prepareStatement(sql);
        ResultSet rs = ps.executeQuery()) {

      sender.sendMessage("§a--- Score List (latest 20) ---");
      int count = 0;

      while (rs.next()) {
        int id = rs.getInt("id");
        String name = rs.getString("player_name");
        int score = rs.getInt("score");
        Timestamp ts = rs.getTimestamp("registered_at");
        String when = (ts != null) ? ts.toLocalDateTime().format(fmt) : "-";

        sender.sendMessage(id + " | " + name + " | " + score + " | " + when);
        count++;
      }
    } catch (SQLException e) {
      sender.sendMessage("§cスコア一覧の取得に失敗しました: " + e.getMessage());
      e.printStackTrace();
    }
  }

  @Override
  public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
      @NotNull String label, @NotNull String[] args) {

    // ▼ /oreRush list でスコア一覧を表示（直近20件）
    if (args.length > 0 && args[0].equalsIgnoreCase("list")) {
      sendScoreList(sender);
      return true;
    }

    if (!(sender instanceof Player senderplayer)) {
      sender.sendMessage("このコマンドはゲーム内から実行してください。");
      return true;
    }
    if (gameActive) {
      sender.sendMessage("ゲームは既に進行中です！");
      return true;
    }

    this.player = senderplayer;
    this.gameActive = true;
    this.score = 0;

    //oreRushと打つとプレイヤーの装備を整え、採掘場所へワープする
    startGame(senderplayer);
    //制限時間を10秒に設定し、ゲーム終了時にメッセージを出す
    startTimer(); return true;
  }

  private static void startGame(Player senderPlayer) {
    senderPlayer.setHealth(20);
    senderPlayer.setFoodLevel(20);

    PlayerInventory inventory = senderPlayer.getInventory();
    inventory.setHelmet(new ItemStack(Material.IRON_HELMET));
    inventory.setChestplate(new ItemStack(Material.IRON_CHESTPLATE));
    inventory.setLeggings(new ItemStack(Material.IRON_LEGGINGS));
    inventory.setBoots(new ItemStack(Material.IRON_BOOTS));
    inventory.setItemInMainHand(new ItemStack(Material.IRON_PICKAXE));

    senderPlayer.sendTitle(
        "ゲームスタート", "鉱石をたくさん掘れ", 0, 30, 0);

    Location location = new Location(senderPlayer.getWorld(), 0, 110, 0);
    senderPlayer.teleport(location);
  }

  // ゲーム終了＋後片付け（DB保存込み）
  private void finishGameAndCleanup() {
    Player p = this.player;

    try {
      if (timerTask != null) {
        timerTask.cancel();
        timerTask = null;
      }

      if (p != null) {
        p.sendTitle("ゲームが終了しました！", "合計" + score + "点！", 0, 60, 0);
        insertScore(p.getName(), score);
      }
    } catch (Exception e) {
      oreRush.getLogger().warning("スコア保存に失敗: " + e.getMessage());
    } finally {
      gameActive = false;
      player = null;
      score = 0;
    }
  }

  private void startTimer() {

    this.remainingTime = GAME_TIME;

    this.timerTask = new BukkitRunnable() {
      @Override
      public void run() {
        remainingTime--;
        if (remainingTime <= 0) {
          cancel();
          finishGameAndCleanup();
        }
      }
    }.runTaskTimer(oreRush, 0L, 20L);
  }

  //鉱石ごとに点数をカウントする
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

  //oreRush listコマンドにてスコアを閲覧
  private void insertScore(String playerName, int totalScore) {
    String url  = oreRush.getConfig().getString(
        "database.url",
        "jdbc:mysql://localhost:3306/spigot_server2?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=Asia/Tokyo&characterEncoding=utf8"
    );
    String user = oreRush.getConfig().getString("database.user", "root");
    String pass = oreRush.getConfig().getString("database.password", "1467Ouninn/");

    String sql = "INSERT INTO player_score (player_name, score, registered_at) " +
        "VALUES (?, ?, CURRENT_TIMESTAMP)";

    try (Connection con = DriverManager.getConnection(url, user, pass);
        PreparedStatement ps = con.prepareStatement(sql)) {

      ps.setString(1, playerName);
      ps.setInt(2, totalScore);
      ps.executeUpdate();

    } catch (SQLException e) {
      e.printStackTrace();
    }
  }

}