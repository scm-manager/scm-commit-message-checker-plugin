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

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

public class RegExValidator implements ConstraintValidator<RegEx, String> {

  @Override
  public void initialize(RegEx constraintAnnotation) {
    // do nothing since we don't need this
  }

  @Override
  public boolean isValid(String object, ConstraintValidatorContext constraintContext) {
    if (object == null || object.isEmpty()) {
      return false;
    }
    try {
      Pattern.compile(object);
      return true;
    } catch (PatternSyntaxException e) {
      constraintContext.buildConstraintViolationWithTemplate(e.getMessage()).addConstraintViolation();
      return false;
    }
  }
}
