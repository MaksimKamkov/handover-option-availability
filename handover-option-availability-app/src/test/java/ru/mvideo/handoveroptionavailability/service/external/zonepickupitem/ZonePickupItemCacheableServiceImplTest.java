package ru.mvideo.handoveroptionavailability.service.external.zonepickupitem;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.when;

import java.time.Duration;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;
import ru.mvideo.handoveroptionavailability.config.CacheProperties;
import ru.mvideo.handoveroptionavailability.mapper.HandoverObjectMapper;
import ru.mvideo.handoveroptionavailability.processor.model.HandoverObject;
import ru.mvideo.io.pickup.points.lib.model.enums.BrandEnum;
import ru.mvideo.lards.geospatial.model.GeoPoint;
import ru.mvideo.lards.pickup.item.service.ZonePickupItemClientApi;
import ru.mvideo.lards.zone.pickup.item.model.HandoverPickupObjectResponse;
import ru.mvideo.lards.zone.pickup.item.model.ObjectType;
import ru.mvideo.lards.zone.pickup.item.model.PickupHandoverFilter;
import ru.mvideo.lards.zone.pickup.item.model.ZoneHandoverPickupObjectResponse;

@ExtendWith(MockitoExtension.class)
class ZonePickupItemCacheableServiceImplTest {

	@Spy
	private HandoverObjectMapper mapper = Mappers.getMapper(HandoverObjectMapper.class);

	@Mock
	private CacheProperties properties;

	@Mock
	private ZonePickupItemClientApi client;

	@InjectMocks
	private ZonePickupItemCacheableServiceImpl service;

	@BeforeEach
	public void setUp() throws Exception {
		when(properties.getHandoverOptionZoneObjectsTtl()).thenReturn(Duration.ofSeconds(60));
		service.afterPropertiesSet();
	}

	@Test
	void getObjectsByZones() {
		var objectResponse = new HandoverPickupObjectResponse("objectId", ObjectType.SHOP, new GeoPoint(1.0, 2.0));
		var response = new ZoneHandoverPickupObjectResponse("zoneId", List.of(objectResponse));
		List<HandoverObject> expected = mapper.toHandoverObjects(List.of(response));
		when(client.getObjectsByZones(anyList(), any(PickupHandoverFilter.class)))
				.thenReturn(Flux.just(response));

		StepVerifier.create(service.getObjectsByZones("regionId", BrandEnum.MVIDEO.getType()))
				.expectNext(expected)
				.verifyComplete();
	}
}