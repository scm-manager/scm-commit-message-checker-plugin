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

package com.cloudogu.scm.commitmessagechecker.cli;

import com.cloudogu.scm.commitmessagechecker.AvailableValidators;
import com.cloudogu.scm.commitmessagechecker.Context;
import com.cloudogu.scm.commitmessagechecker.InvalidCommitMessageException;
import com.cloudogu.scm.commitmessagechecker.Validator;
import com.cloudogu.scm.commitmessagechecker.config.Configuration;
import com.cloudogu.scm.commitmessagechecker.config.ConfigurationProvider;
import com.cloudogu.scm.commitmessagechecker.config.Validation;
import com.google.common.annotations.VisibleForTesting;
import picocli.CommandLine;
import sonia.scm.cli.CliContext;
import sonia.scm.cli.CliResourceBundle;
import sonia.scm.cli.ParentCommand;
import sonia.scm.repository.NamespaceAndName;
import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryManager;
import sonia.scm.repository.cli.RepositoryCommand;

import jakarta.inject.Inject;
import java.util.List;

@ParentCommand(RepositoryCommand.class)
@CliResourceBundle("sonia.scm.cmc.cli.i18n")
@CommandLine.Command(name = "commit-message-check", aliases = "cmc")
public class CommitMessageCheckerCommand implements Runnable {

  @CommandLine.Parameters(paramLabel = "namespace/name", descriptionKey = "scm.repo.cmc.repository", index = "0")
  private String repository;

  @CommandLine.Parameters(paramLabel = "branch", descriptionKey = "scm.repo.cmc.branch", index = "1")
  private String branch;

  @CommandLine.Parameters(paramLabel = "commit-message", descriptionKey = "scm.repo.cmc.commitMessage", index = "2")
  private String commitMessage;

  @CommandLine.Mixin
  private final CommitMessageCheckerTemplateRenderer templateRenderer;
  private final CliContext context;

  private final ConfigurationProvider configurationProvider;
  private final AvailableValidators availableValidators;
  private final RepositoryManager repositoryManager;

  @Inject
  public CommitMessageCheckerCommand(
    CommitMessageCheckerTemplateRenderer templateRenderer,
    CliContext context,
    ConfigurationProvider configurationProvider,
    AvailableValidators availableValidators,
    RepositoryManager repositoryManager
  ) {
    this.templateRenderer = templateRenderer;
    this.context = context;
    this.configurationProvider = configurationProvider;
    this.availableValidators = availableValidators;
    this.repositoryManager = repositoryManager;
  }

  @Override
  public void run() {
    String[] splitRepo = repository.split("/");
    Repository repo = repositoryManager.get(new NamespaceAndName(splitRepo[0], splitRepo[1]));
    if (repo == null) {
      templateRenderer.renderNotFoundError();
      return;
    }
    configurationProvider.evaluateConfiguration(repo)
      .ifPresent(
        configuration -> {
          try {
            validate(configuration, repo);
          } catch (InvalidCommitMessageException e) {
            templateRenderer.renderDefaultError(e);
            context.getStderr().println();
            context.exit(2);
          }
        });
  }

  private void validate(Configuration configuration, Repository repo) {
    List<Validation> validations = configuration.getValidations();
    for (Validation validation : validations) {
      Validator validator = availableValidators.validatorFor(validation.getName());
      validator.validate(new Context(repo, branch, validation.getConfiguration()), commitMessage);
    }
  }

  @VisibleForTesting
  void setRepository(String repository) {
    this.repository = repository;
  }
  @VisibleForTesting
  void setCommitMessage(String commitMessage) {
    this.commitMessage = commitMessage;
  }
}
