package ru.mvideo.handoveroptionavailability.runner;

import io.cucumber.junit.Cucumber;
import io.cucumber.junit.CucumberOptions;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import ru.mvideo.btt.mock.manager.WiremockManager;

@RunWith(Cucumber.class)
@CucumberOptions(
		plugin = {"io.qameta.allure.cucumber6jvm.AllureCucumber6Jvm", "pretty"},
		features = "classpath:features",
		glue = {"ru.mvideo.btt.core.stepdefinitions",
				"ru.mvideo.btt.core.configuration",
				"ru.mvideo.btt.api.stepdefinitions",
				"ru.mvideo.btt.mock.stepdefinitions"},
		tags = "@handover-option-availability"
)
public class IntegrationTestRunner {
	@BeforeClass
	public static void setUp() {
		WiremockManager.setUpWiremock();
	}
}