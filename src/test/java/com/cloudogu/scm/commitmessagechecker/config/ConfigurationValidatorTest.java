/*
 * Copyright (c) 2020 - present Cloudogu GmbH
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the Free
 * Software Foundation, version 3.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/.
 */

package com.cloudogu.scm.commitmessagechecker.config;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.junit.jupiter.api.Test;

import jakarta.validation.ConstraintViolationException;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

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
