package tlm.dev.basalt.demo.config;

public record RunSettings(int maxRuns, int maxPlayers, double monsterHealth, int returnDelaySeconds) {

    public RunSettings {
        if (maxRuns < 1 || maxPlayers < 1 || returnDelaySeconds < 0) {
            throw new IllegalArgumentException("runs and players are at least 1, the return delay at least 0");
        }
        if (!(monsterHealth > 0 && monsterHealth <= 1024)) {
            throw new IllegalArgumentException("a monster health is above 0 and at most 1024: " + monsterHealth);
        }
    }
}
