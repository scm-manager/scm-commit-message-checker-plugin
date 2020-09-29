package com.cloudogu.scm.commitmessagechecker.config;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.junit.jupiter.api.Test;

import javax.validation.ConstraintViolationException;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import static org.junit.jupiter.api.Assertions.assertThrows;

class ConfigurationValidatorTest {

  ConfigurationValidator validator = new ConfigurationValidator();

  @Test
  void shouldSuccessfullyValidate() {
    validator.validate(new ValidatorConfig("abc", "master"));
  }

  @Test
  void validationShouldFail() {
    assertThrows(ConstraintViolationException.class, () -> validator.validate(new ValidatorConfig(null, "")));
  }

  @AllArgsConstructor
  @Getter
  static class ValidatorConfig {
    @NotNull
    private final String pattern;
    @NotBlank
    private final String branches;
  }

}
