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

import javax.inject.Inject;
import java.util.Set;

public class AvailableValidators {

  private final Set<Validator> validators;

  @Inject
  public AvailableValidators(Set<Validator> validators) {
    this.validators = validators;
  }

  public Validator validatorFor(String name) {
    return validators.stream()
      .filter(validator -> validator.getClass().getSimpleName().equals(name))
      .findFirst()
      .orElseThrow(() -> new UnknownValidatorException(name));
  }

  public static String nameOf(Validator validator) {
    return nameOf(validator.getClass());
  }

  public static String nameOf(Class<? extends Validator> validatorClass) {
    return validatorClass.getSimpleName();
  }

}
