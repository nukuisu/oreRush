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

  public static final int GAME_TIME = 30;
  private BukkitTask timerTask;
  private GameSession currentSession;
  private int sessionCounter = 0;

  public CommandOreRush(OreRush oreRush) {
    this.oreRush = oreRush;
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

    // oreRush list でスコア一覧を表示（直近5件）
    if (args.length > 0 && args[0].equalsIgnoreCase("list")) {
      sendScoreList(sender);
      return true;
    }

    if (!(sender instanceof Player senderplayer)) {
      sender.sendMessage("このコマンドはゲーム内から実行してください。");
      return true;
    }
    if (currentSession !=null && currentSession.active) {
      sender.sendMessage("ゲームは既に進行中です！");
      return true;
    }else {
      sessionCounter++;
      currentSession = new GameSession(sessionCounter, senderplayer, GAME_TIME);
    }

    //oreRushと打つとプレイヤーの装備を整え、採掘場所へワープする
    startGame();
    //制限時間を10秒に設定し、ゲーム終了時にメッセージを出す
    startTimer(); return true;
  }

  private void startGame() {
    currentSession.preparePlayer();
  }

  private void startTimer() {

    GameSession session = this.currentSession;

    this.timerTask = new BukkitRunnable() {
      @Override
      public void run() {
        // 最新のゲームでなければ何もしない
        if (session != currentSession) { cancel(); return; }

        session.remainingTime--;

        if (session.remainingTime == 10) {
          session.player.sendMessage("残り10秒です");
        }

        if (session.remainingTime <= 0) {
          cancel();
          // ここでもう一度チェック
          if (session.remainingTime <= 0) { cancel(); finishGameAndCleanup(session); }
        }
      }
    }.runTaskTimer(oreRush, 0L, 20L);

  }

  // ゲーム終了＋後片付け（DB保存込み）
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

  //鉱石ごとに点数をカウントする
  @EventHandler
  public void onBlockBreak(BlockBreakEvent b ) {

    GameSession session = this.currentSession;

    if (session == null || !session.active) return;
    if (session.player == null) return;
    if (!b.getPlayer().getUniqueId().equals(session.player.getUniqueId())) return;

    Block block = b.getBlock();
    Material type = block.getType();

    int point = session.getPoint(type);

    if (point > 0) {
      session.score += point;
      session.player.sendMessage("採掘完了！現在のスコア: " + session.score + "点");
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