package com.cloudogu.scm.commitmessagechecker;

import com.google.common.collect.ImmutableSet;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AvailableValidatorsTest {

  AvailableValidators availableValidators = new AvailableValidators(ImmutableSet.of(new TestValidator()));

  @Test
  void shouldGetValidatorByName() {
    Validator validator = availableValidators.validatorOf("TestValidator");

    assertThat(validator).isInstanceOf(TestValidator.class);
  }

  @Test
  void shouldThrowUnknownValidatorException() {
    assertThrows(UnknownValidatorException.class, () -> availableValidators.validatorOf("Unknown"));
  }

  @Test
  void shouldGetNameByValidator() {
    String name = AvailableValidators.nameOf(new TestValidator());
    assertThat(name).isEqualTo("TestValidator");
  }

  @Test
  void shouldGetNameByValidatorClass() {
    String name = AvailableValidators.nameOf(TestValidator.class);
    assertThat(name).isEqualTo("TestValidator");
  }

  static class TestValidator implements Validator {

    @Override
    public boolean isApplicableMultipleTimes() {
      return false;
    }

    @Override
    public Optional<Class<?>> getConfigurationType() {
      return Optional.empty();
    }

    @Override
    public void validate(Context context, String commitMessage) {
      // Do nothing
    }
  }
}
