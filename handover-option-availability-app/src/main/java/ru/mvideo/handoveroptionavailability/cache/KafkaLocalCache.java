package ru.mvideo.handoveroptionavailability.cache;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import java.util.Map;
import java.util.function.Function;
import javax.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.data.util.Pair;
import reactor.core.Disposable;
import ru.mvideo.dataflow.consumer.KafkaAutoAcknowledgeConsumer;
import ru.mvideo.dataflow.model.KafkaAutoAckRecord;

@Slf4j
public abstract class KafkaLocalCache<C, K, V> {

	private final KafkaAutoAcknowledgeConsumer<C> consumer;
	private final Function<KafkaAutoAckRecord<C>, Pair<K, V>> dataTransformFn;
	private final Cache<K, V> cache;
	private Disposable disposable;


	public KafkaLocalCache(KafkaAutoAcknowledgeConsumer<C> consumer,
	                       Function<KafkaAutoAckRecord<C>, Pair<K, V>> dataTransformFn) {
		this.consumer = consumer;
		this.dataTransformFn = dataTransformFn;
		this.cache = Caffeine.newBuilder().recordStats().build();
	}


	@EventListener(ApplicationReadyEvent.class)
	public void processObjectData() {
		disposable = consumer.getData()
				.filter(this::isDataApplicable)
				.map(dataTransformFn)
				.doOnNext(p -> cache.put(p.getFirst(), p.getSecond()))
				.retry()
				.onErrorContinue(this::onError)
				.subscribe();
	}

	@PreDestroy
	public void destroy() {
		disposable.dispose();
	}

	public V get(K key) {
		return cache.getIfPresent(key);
	}

	public Map<K, V> getAll(Iterable<K> keys) {
		return cache.getAllPresent(keys);
	}

	protected abstract boolean isDataApplicable(KafkaAutoAckRecord<C> record);

	protected void onError(Throwable throwable, Object object) {
		log.error("Error during data update", throwable);
	}
}
