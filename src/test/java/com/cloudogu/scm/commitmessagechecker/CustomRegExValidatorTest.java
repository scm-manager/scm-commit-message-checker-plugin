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
  void shouldValidateWithoutBranchesConfigured() {
    String regex = "^[A-Za-z ]+$";
    CustomRegExValidatorConfig config = new CustomRegExValidatorConfig(regex, null, "it's gonna explode");
    assertThrows(InvalidCommitMessageException.class,
      () -> validator.validate(new Context(REPOSITORY, "feature/123", config), "invalid message 123!"));
  }

  @Test
  void shouldValidateWithoutCommitBranch() {
    String regex = "^[A-Za-z ]+$";
    CustomRegExValidatorConfig config = new CustomRegExValidatorConfig(regex, "", "");
    assertThrows(InvalidCommitMessageException.class,
      () -> validator.validate(new Context(REPOSITORY, "", config), "invalid message 123!"));
  }

  @Test
  void shouldAcceptPartialExpression() {
    String regex = "#[0-9]+";
    CustomRegExValidatorConfig config = new CustomRegExValidatorConfig(regex, "master", "Need ticket number");
    validator.validate(new Context(REPOSITORY, "master", config), "Ticket #42 fixed");
  }
}
