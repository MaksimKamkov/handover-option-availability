package ru.mvideo.handoveroptionavailability.processor.handler;

import java.util.List;
import javax.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import ru.mvideo.handoveroptionavailability.cache.LocalCache;
import ru.mvideo.handoveroptionavailability.config.CacheProperties;
import ru.mvideo.handoveroptionavailability.model.BriefOptions;
import ru.mvideo.handoveroptionavailability.model.BriefRequest;

@Component
@RequiredArgsConstructor
public class BriefCache {

	private final CacheProperties cacheProperties;
	private final BriefHandler briefHandler;

	private LocalCache<BriefRequest, List<BriefOptions>> cache;

	@PostConstruct
	public void setup() {
		this.cache = new LocalCache<>(cacheProperties.getHandoverOptionBriefTtl(), request -> handle(request).collectList());
	}

	public Flux<BriefOptions> handoverOptions(BriefRequest request) {
		if (Boolean.TRUE.equals(request.getCachebust())) {
			return cache.get(request).flatMapMany(Flux::fromIterable);
		} else {
			return handle(request);
		}
	}

	private Flux<BriefOptions> handle(BriefRequest request) {
		return briefHandler.handle(request);
	}
}
