package ru.mvideo.handoveroptionavailability.cache;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@ExtendWith(MockitoExtension.class)
public class LocalCacheTest {

	@Test
	public void shouldHitCache() {
		final var counter = new AtomicInteger(0);

		final Function<String, Mono<String>> fn = (k) -> Mono.fromCallable(() -> {
			counter.incrementAndGet();
			return String.format("Cached %s", k);
		});
		final var cache = new LocalCache<>(Duration.ofMinutes(1), fn);

		StepVerifier.create(cache.get("One"))
				.assertNext(result -> Assertions.assertThat(result)
						.isEqualTo("Cached One"))
				.verifyComplete();
		StepVerifier.create(cache.get("One"))
				.assertNext(result -> Assertions.assertThat(result)
						.isEqualTo("Cached One"))
				.verifyComplete();

		Assertions.assertThat(counter.get()).isEqualTo(1);
	}

	@Test
	public void shouldMissCache() {
		final var counter = new AtomicInteger(0);

		final Function<String, Mono<String>> fn = (k) -> Mono.fromCallable(() -> {
			counter.incrementAndGet();
			return String.format("Cached %s", k);
		});
		final var cache = new LocalCache<>(Duration.ofMinutes(1), fn);

		StepVerifier.create(cache.get("One"))
				.assertNext(result -> Assertions.assertThat(result)
						.isEqualTo("Cached One"))
				.verifyComplete();
		StepVerifier.create(cache.get("Two"))
				.assertNext(result -> Assertions.assertThat(result)
						.isEqualTo("Cached Two"))
				.verifyComplete();

		Assertions.assertThat(counter.get()).isEqualTo(2);
	}
}
