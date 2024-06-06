package ru.mvideo.handoveroptionavailability.processor.model;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.Data;
import lombok.Singular;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.collections4.SetUtils;
import ru.mvideo.handoveroptionavailability.model.Material;
import ru.mvideo.handoveroptionavailability.model.RetailBrand;
import ru.mvideo.handoveroptionavailability.model.StockObject;
import ru.mvideo.handoveroptionavailability.model.external.msp.logistic.response.AvailabilityOption;
import ru.mvideo.handoveroptionavailability.processor.filter.fit.ExtendedProduct;
import ru.mvideo.lards.handover.option.model.ProviderZoneAttributesHandoverOptions;
import ru.mvideo.lards.price.rules.model.MinPriceResponse;
import ru.mvideo.lards.zone.model.ZoneDetailResponse;
import ru.mvideo.lards.zone.model.ZoneResponse;

@Slf4j
@Accessors(fluent = true, chain = true)
@Data
public abstract class Context {

	private final Set<String> requestHandoverOptions;

	private final Set<String> options;

	private Map<String, OptionContext> handoverOptionContext;

	@Singular
	private final Set<Flags> flags = new HashSet<>();

	private String regionId;
	private RetailBrand retailBrand;
	private List<Material> materials;
	private Set<String> uniqueMaterials;

	private ZoneDetailResponse regionDetails;
	private List<ZoneResponse> zones;
	private Set<String> zoneIds;

	private List<ProviderZoneAttributesHandoverOptions> providers;
	private List<MinPriceResponse> minPriceRules;

	private List<ExtendedProduct> products;

	private final Set<String> pickupObjectIds = new HashSet<>();
	private final Set<String> sapCodes = new HashSet<>();
	private List<AvailabilityOption> availabilityOptions;

	private List<HandoverObject> handoverObjects;
	private Map<String, StockObject> stockObjects;
	private Set<String> stockHandoverObjects;

	private String paymentMethod;
	private Integer creditApprovalLeadTime;
	private Integer minStock;

	public Set<String> requestHandoverOptions() {
		return Collections.unmodifiableSet(requestHandoverOptions);
	}

	public void addFlag(Flags flag) {
		flags.add(flag);
	}

	public Set<Flags> flags() {
		return Collections.unmodifiableSet(flags);
	}

	public void disableOption(String option, String reason) {
		if (requestHandoverOptions.contains(option)) {
			log.warn("Disable option {}. Reason: {}", option, reason);
		}
		options.remove(option);
	}

	public void disableOptions(List<String> options, String reason) {
		for (String option : options) {
			if (this.options.contains(option)) {
				log.warn("Disable option {}. Reason: {}", option, reason);
			}
			this.options.remove(option);
		}
	}

	public Set<String> uniqueMaterials() {
		return Objects.requireNonNullElseGet(uniqueMaterials, () -> {
			uniqueMaterials = materials.stream().map(Material::getMaterial).collect(Collectors.toSet());
			return uniqueMaterials;
		});
	}

	public boolean hasOption(String option) {
		return options.contains(option);
	}

	public Collection<String> options() {
		return options;
	}

	public Context(Set<String> requestHandoverOptions, Map<String, OptionContext> handoverOptionContext, String regionId,
	               RetailBrand retailBrand, List<Material> materials) {
		this(requestHandoverOptions, handoverOptionContext, regionId, retailBrand, materials, Collections.emptySet(), Collections.emptyList());
	}

	public Context(Set<String> requestHandoverOptions, Map<String, OptionContext> handoverOptionContext, String regionId,
	               RetailBrand retailBrand, List<Material> materials, Set<String> stockHandoverObjects, List<StockObject> stockObjects) {
		this.requestHandoverOptions = requestHandoverOptions;
		this.handoverOptionContext = handoverOptionContext;
		this.regionId = regionId;
		this.retailBrand = retailBrand;
		this.materials = materials;
		this.options = new CopyOnWriteArraySet<>(Objects.requireNonNullElse(requestHandoverOptions, Collections.emptySet()));
		this.stockHandoverObjects = SetUtils.emptyIfNull(stockHandoverObjects);
		this.stockObjects = ListUtils.emptyIfNull(stockObjects).stream()
				.collect(Collectors.toMap(StockObject::getStock, Function.identity()));
	}
}
