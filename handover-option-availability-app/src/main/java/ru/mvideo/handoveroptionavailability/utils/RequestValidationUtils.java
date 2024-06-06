package ru.mvideo.handoveroptionavailability.utils;

import java.util.List;
import lombok.experimental.UtilityClass;
import org.apache.commons.collections4.CollectionUtils;
import ru.mvideo.handoveroptionavailability.exception.DuplicateMaterialsException;
import ru.mvideo.handoveroptionavailability.exception.QtyMaterialsException;
import ru.mvideo.handoveroptionavailability.model.Material;
import ru.mvideo.handoveroptionavailability.model.MaterialWithoutQty;

@UtilityClass
public class RequestValidationUtils {

	public void requireUniqueMaterials(List<Material> materials) {
		if (CollectionUtils.isNotEmpty(materials) && materials.stream().map(Material::getMaterial).distinct().count() != materials.size()) {
			throw new DuplicateMaterialsException();
		}
	}

	public void requireUniqueMaterialsWithoutQty(List<MaterialWithoutQty> materials) {
		if (CollectionUtils.isNotEmpty(materials) && materials.stream().map(MaterialWithoutQty::getMaterial).distinct().count() != materials.size()) {
			throw new DuplicateMaterialsException();
		}
	}

	public void requireQtyMaterials(List<Material> materials) {
		if (materials.stream().mapToInt(Material::getQty).sum() > 100) {
			throw new QtyMaterialsException();
		}
	}
}
