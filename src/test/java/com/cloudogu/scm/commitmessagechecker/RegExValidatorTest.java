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

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import jakarta.validation.ConstraintValidatorContext;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class RegExValidatorTest {

  @Mock(answer = Answers.RETURNS_DEEP_STUBS)
  private ConstraintValidatorContext constraintValidatorContext;

  private final RegExValidator validator = new RegExValidator();

  @Test
  void valid() {
    boolean valid = validator.isValid("^[A-Z ]$", constraintValidatorContext);
    assertThat(valid).isTrue();
  }

  @Test
  void invalid() {
    boolean valid = validator.isValid("^[[$", constraintValidatorContext);
    assertThat(valid).isFalse();
  }
}
