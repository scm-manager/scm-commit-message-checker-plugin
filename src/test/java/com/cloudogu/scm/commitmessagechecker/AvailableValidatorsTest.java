/*
 * MIT License
 *
 * Copyright (c) 2020-present Cloudogu GmbH and Contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
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
