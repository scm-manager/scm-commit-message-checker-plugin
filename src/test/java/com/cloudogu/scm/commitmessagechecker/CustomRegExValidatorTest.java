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

import com.cloudogu.scm.commitmessagechecker.CustomRegExValidator.CustomRegExValidatorConfig;
import org.junit.jupiter.api.Test;
import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryTestData;

import static org.junit.jupiter.api.Assertions.assertThrows;

class CustomRegExValidatorTest {

  private static final Repository REPOSITORY = RepositoryTestData.createHeartOfGold();
  private final CustomRegExValidator validator = new CustomRegExValidator();

  @Test
  void shouldSuccessfullyValidate() {
    String regex = "^[A-Za-z ]+$";
    CustomRegExValidatorConfig config = new CustomRegExValidatorConfig(regex, "master,develop", "don't panic");
    validator.validate(new Context(REPOSITORY, "master", config), "awesome features incoming");
  }

  @Test
  void shouldOnlyValidateConfiguredBranches() {
    String regex = "^[A-Za-z ]+$";
    CustomRegExValidatorConfig config = new CustomRegExValidatorConfig(regex, "develop", "don't panic");
    validator.validate(new Context(REPOSITORY, "master", config), "invalid message 123!");
  }

  @Test
  void shouldValidateBranchesWithWildcardBranchName() {
    String regex = "^[A-Za-z ]+$";
    CustomRegExValidatorConfig config = new CustomRegExValidatorConfig(regex, "feature/*", "it's gonna explode");
    assertThrows(InvalidCommitMessageException.class,
      () -> validator.validate(new Context(REPOSITORY, "feature/123", config), "invalid message 123!"));
  }

  @Test
  void shouldNotValidateCommitsIfBranchesNotConfigured() {
    String regex = "^[A-Za-z ]+$";
    CustomRegExValidatorConfig config = new CustomRegExValidatorConfig(regex, "", "it's gonna explode");
    validator.validate(new Context(REPOSITORY, "feature/123", config), "invalid message 123!");
  }
}
