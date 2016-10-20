package se.gustavkarlsson.aurora_notifier.web_service;


import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import java.time.Duration;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.LoggerFactory;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

public class AuroraConfigurationTest {

	private static Validator validator;

	private AuroraConfiguration config;

	@BeforeClass
	public static void setupClass() {
		validator = Validation.buildDefaultValidatorFactory().getValidator();
	}

	@Before
	public void setup() {
		config = new AuroraConfiguration();
	}

	@Test
	public void defaultConfigurationIsValid() {
		assertThat(validator.validate(config)).isEmpty();
	}

	@Test
	public void negativeCacheDurationIsInvalid() {
		config.setKpIndexCacheDuration(Duration.ofMillis(-1));
		validateCacheDurationToShort();
	}

	@Test
	public void shortCacheDurationIsInvalid() {
		config.setKpIndexCacheDuration(Duration.ofMillis(1));
		validateCacheDurationToShort();
	}

	@Test
	public void oneMinuteCacheDurationIsValid() {
		config.setKpIndexCacheDuration(Duration.ofMinutes(1));
		assertThat(validator.validate(config)).isEmpty();
	}

	@Test
	public void zeroCacheDurationIsInvalid() {
		config.setKpIndexCacheDuration(Duration.ofMillis(0));
		validateCacheDurationToShort();
	}

	private void validateCacheDurationToShort() {
		Set<ConstraintViolation<AuroraConfiguration>> violations = validator.validate(config);
		assertThat(violations).hasSize(1);
		ConstraintViolation<AuroraConfiguration> violation = violations.iterator().next();
		assertThat(violation.getPropertyPath().toString()).isEqualTo("kpIndexCacheDurationValid");
		assertThat(violation.getMessage()).isEqualTo(AuroraConfiguration.AT_LEAST_ONE_MINUTE);
	}
}
