package ru.mvideo.handoveroptionavailability.cache;

import com.github.benmanes.caffeine.cache.AsyncLoadingCache;
import com.github.benmanes.caffeine.cache.Caffeine;
import java.time.Duration;
import java.util.function.Function;
import javax.validation.constraints.NotNull;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

public class LocalCache<K, V> {

	private final Function<K, Mono<V>> cacheFn;

	public LocalCache(Duration duration, Function<K, Mono<V>> fn) {
		cacheFn = ofMono(duration, fn);
	}

	public Mono<V> get(K key) {
		return cacheFn.apply(key);
	}

	private Function<K, Mono<V>> ofMono(@NotNull Duration duration, Function<K, Mono<V>> fn) {
		final AsyncLoadingCache<K, V> cache = Caffeine.newBuilder()
				.expireAfterWrite(duration)
				.recordStats()
				.buildAsync((k, e) ->
						fn.apply(k)
								.subscribeOn(Schedulers.fromExecutor(e))
								.toFuture());

		return (k) -> Mono.fromFuture(cache.get(k));
	}
}
