package ru.mvideo.handoveroptionavailability;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.context.annotation.Import;
import org.springframework.scheduling.annotation.EnableScheduling;
import ru.mvideo.lards.packing.algorithm.PackageAlgorithmConfig;

@EnableScheduling
@ConfigurationPropertiesScan
@Import(PackageAlgorithmConfig.class)
@SpringBootApplication
public class Application {

	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}
}