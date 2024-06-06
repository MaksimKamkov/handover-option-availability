package ru.mvideo.handoveroptionavailability.config;

import java.util.Set;
import lombok.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Value
@ConfigurationProperties(prefix = "handover-option-availability.config.materials")
public class MaterialsProperty {

	Set<String> requiredLabelGroups;
}