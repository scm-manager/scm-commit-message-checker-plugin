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
    Validator validator = availableValidators.validatorFor("TestValidator");

    assertThat(validator).isInstanceOf(TestValidator.class);
  }

  @Test
  void shouldThrowUnknownValidatorException() {
    assertThrows(UnknownValidatorException.class, () -> availableValidators.validatorFor("Unknown"));
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
