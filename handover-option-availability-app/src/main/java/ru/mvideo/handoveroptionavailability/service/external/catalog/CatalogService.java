package ru.mvideo.handoveroptionavailability.service.external.catalog;

import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import ru.mvideo.product.client.CatalogClientV2;
import ru.mvideo.product.model.BlocksEnum;
import ru.mvideo.product.model.ProductDto;

@Slf4j
@Service
@RequiredArgsConstructor
public class CatalogService {

	/**
	 * Product blocks, we need to request from catalog service.
	 */
	private static final List<BlocksEnum> RQ_PRODUCT_BLOCKS =
			List.of(
					BlocksEnum.FLAGS,
					BlocksEnum.SAPATTRIBUTES,
					BlocksEnum.GROUPIDS,
					BlocksEnum.COMPLEMENT
			);

	private final CatalogClientV2 client;

	public Mono<List<ProductDto>> getProductsRaw(List<String> materials) {
		return client.getProducts(materials, RQ_PRODUCT_BLOCKS)
				.onErrorResume(
						Exception.class,
						fallback -> {
							log.error("Catalog service error: {}", fallback.getMessage());
							return Mono.empty();
						})
				.collectList();
	}
}
