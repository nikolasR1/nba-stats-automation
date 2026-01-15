package core;

public class PlayerGamePerformance {
    private Player player;
    private Game game;
    private StatLine stats;

    public PlayerGamePerformance(Player player, Game game, StatLine stats) {
        this.player = player;
        this.game = game;
        this.stats = stats;
    }

    public Player getPlayer() { return player; }
    public Game getGame() { return game; }
    public StatLine getStats() { return stats; } // <-- add this
}
