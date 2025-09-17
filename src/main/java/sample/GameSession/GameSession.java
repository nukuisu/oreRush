package sample.GameSession;

import org.bukkit.entity.Player;

 public class GameSession {
  public final int id;
  public boolean active;
  public int score;
  public int remainingTime;
  public Player player;

  public GameSession(int id, Player player, int gameTime) {
    this.id = id;
    this.player = player;
    this.active = true;
    this.score = 0;
    this.remainingTime = gameTime;
  }
}
