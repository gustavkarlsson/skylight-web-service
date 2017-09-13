package se.gustavkarlsson.aurora_notifier.web_service;


import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import java.time.Duration;
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
	public void negativeUpdateDelayIsInvalid() {
		config.setUpdateDelay(Duration.ofMillis(-1));
		validateUpdateDelayToShort();
	}

	@Test
	public void shortUpdateDelayIsInvalid() {
		config.setUpdateDelay(Duration.ofMillis(1));
		validateUpdateDelayToShort();
	}

	@Test
	public void oneMinuteUpdateDelayIsValid() {
		config.setUpdateDelay(Duration.ofMinutes(1));
		assertThat(validator.validate(config)).isEmpty();
	}

	@Test
	public void zeroUpdateDelayIsInvalid() {
		config.setUpdateDelay(Duration.ofMillis(0));
		validateUpdateDelayToShort();
	}

	private void validateUpdateDelayToShort() {
		Set<ConstraintViolation<AuroraConfiguration>> violations = validator.validate(config);
		assertThat(violations).hasSize(1);
		ConstraintViolation<AuroraConfiguration> violation = violations.iterator().next();
		assertThat(violation.getPropertyPath().toString()).isEqualTo("updateDelayValid");
		assertThat(violation.getMessage()).isEqualTo(AuroraConfiguration.AT_LEAST_ONE_MINUTE);
	}
}
