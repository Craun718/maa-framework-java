package io.github.craun718.maafw;

/** A gamescope instance discovered by {@link Toolkit#findGamescopeInstances()}. */
public record GamescopeInstance(int displayNo, int pipewireNodeId, String eisSocketPath) {
}
