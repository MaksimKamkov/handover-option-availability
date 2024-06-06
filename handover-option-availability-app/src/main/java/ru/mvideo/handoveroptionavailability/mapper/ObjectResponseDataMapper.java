package ru.mvideo.handoveroptionavailability.mapper;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import org.mapstruct.AfterMapping;
import org.mapstruct.Builder;
import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import ru.mvideo.handoveroptionavailability.service.external.objectspublic.kafka.model.ExtendedObjectResponseData;
import ru.mvideo.oi.pbl.model.ObjectAttributeData;
import ru.mvideo.oi.pbl.model.ObjectResponseData;
import ru.mvideo.oi.pbl.model.ObjectScheduleData;

@Mapper(builder = @Builder(disableBuilder = true))
public interface ObjectResponseDataMapper {

	@Mapping(target = "attributes", ignore = true)
	@Mapping(target = "schedules", ignore = true)
	ObjectResponseData toObjectResponseData(ExtendedObjectResponseData extendedObject,
	                                        @Context Collection<String> requiredAttributes);

	@AfterMapping
	default void completeObjectResponseData(@MappingTarget ObjectResponseData target, ExtendedObjectResponseData extendedObject,
	                                        @Context Collection<String> requiredAttributes) {
		target.setAttributes(filterAttributes(extendedObject.getAttributes(), requiredAttributes, ObjectAttributeData::getAttribute));
		target.setSchedules(filterAttributes(extendedObject.getSchedules(), requiredAttributes, ObjectScheduleData::getScheduleId));
	}

	private <T> List<T> filterAttributes(List<T> originalAttributes, Collection<String> requiredAttributes,
	                                     Function<T, String> attributeMapping) {
		if (originalAttributes == null) {
			return Collections.emptyList();
		}
		return originalAttributes.stream()
				.filter(attribute -> requiredAttributes.isEmpty() || requiredAttributes.contains(attributeMapping.apply(attribute)))
				.toList();
	}
}
