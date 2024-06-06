package ru.mvideo.handoveroptionavailability;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

public class ApplicationTest extends AbstractIT {
	@Autowired
	private ApplicationContext applicationContext;

	@Test
	public void contextCreatedTest() {
		assertNotNull(applicationContext);
	}
}
