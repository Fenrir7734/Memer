package com.fenrir.Memer.api;

import java.util.Optional;

public interface MediaProvider<T> {
    Optional<T> getMeme(String source);
    void shutdown();
}
