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
import sample.GameSession.GameSession;
import sample.example.oreRush.OreRush;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.format.DateTimeFormatter;

public class CommandOreRush implements CommandExecutor, Listener {

  private final OreRush oreRush;

  private BukkitTask timerTask;
  private GameSession currentSession;
  private int sessionCounter = 0;

  /**
   *orerushと打つと採掘ゲームが始まる
   */
  public CommandOreRush(OreRush oreRush) {
    this.oreRush = oreRush;
  }

  //orerush listと打つと直近5件分の点数が閲覧できる
  private void sendScoreList(CommandSender sender) {
    String url  = oreRush.getConfig().getString(
        "database.url",
        "jdbc:mysql://localhost:3306/spigot_server2?useSSL=false&serverTimezone=Asia/Tokyo&characterEncoding=utf8");
    String user = oreRush.getConfig().getString("database.user", "root");
    String pass = oreRush.getConfig().getString("database.password", "1467Ouninn/");

    String sql = """
  SELECT id, player_name, score, registered_at
  FROM player_score
  ORDER BY registered_at DESC
  LIMIT 5
  """;

    DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    try (Connection con = DriverManager.getConnection(url, user, pass);
        PreparedStatement ps = con.prepareStatement(sql);
        ResultSet rs = ps.executeQuery()) {

      sender.sendMessage("§a--- Score List (latest 5) ---");

      while (rs.next()) {
        int id = rs.getInt("id");
        String name = rs.getString("player_name");
        int score = rs.getInt("score");
        Timestamp ts = rs.getTimestamp("registered_at");
        String when = (ts != null) ? ts.toLocalDateTime().format(fmt) : "-";

        sender.sendMessage(id + " | " + name + " | " + score + " | " + when);
      }
    } catch (SQLException e) {
      sender.sendMessage("§cスコア一覧の取得に失敗しました: " + e.getMessage());
      e.printStackTrace();
    }
  }


  @Override
  public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
      @NotNull String label, @NotNull String[] args) {

    if (args.length > 0 && args[0].equalsIgnoreCase("list")) {
      sendScoreList(sender);
      return true;
    }
    //違うプレイヤーや連続でorerushと打ったらゲームは止まる
    if (!(sender instanceof Player senderplayer)) {
      sender.sendMessage("このコマンドはゲーム内から実行してください。");
      return true;
    }
    if (currentSession !=null && currentSession.active) {
      sender.sendMessage("ゲームは既に進行中です！");
      return true;
    }else {
      sessionCounter++;
      currentSession = new GameSession(sessionCounter, senderplayer);
    }

    startGame();
    startTimer(); return true;
  }

  private void startGame() {
    currentSession.preparePlayer();
  }

  private void startTimer() {

    if (this.timerTask != null) { this.timerTask.cancel(); this.timerTask = null; }
    final GameSession session = this.currentSession;

    this.timerTask = new BukkitRunnable() {
      @Override
      public void run() {
        if (session != currentSession) { cancel(); return; }

        boolean finished = session.tick();
        if (finished) { cancel(); finishGameAndCleanup(session);}
      }
    }.runTaskTimer(oreRush, 20L, 20L);

  }

  private void finishGameAndCleanup(GameSession session) {
    Player p = session.player;

    try {
      if (timerTask != null) {
        timerTask.cancel();
        timerTask = null;
      }

      if (p != null) {
        p.sendTitle("ゲームが終了しました！", "合計" + session.score + "点！", 0, 60, 0);
        insertScore(p.getName(), session.score);
      }
    } catch (Exception e) {
      oreRush.getLogger().warning("スコア保存に失敗: " + e.getMessage());
    } finally {
      session.active = false;
      session.player = null;
      session.score = 0;
    }
  }

  @EventHandler
  public void onBlockBreak(BlockBreakEvent b ) {

    GameSession session = this.currentSession;

    if (session == null || !session.active) return;
    if (session.player == null) return;
    if (!b.getPlayer().getUniqueId().equals(session.player.getUniqueId())) return;

    session.addScore(b.getBlock());
  }

  //点数をDBへ保存
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