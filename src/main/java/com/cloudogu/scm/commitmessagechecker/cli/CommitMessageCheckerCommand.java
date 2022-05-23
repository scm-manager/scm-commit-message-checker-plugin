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

package com.cloudogu.scm.commitmessagechecker.cli;

import com.cloudogu.scm.commitmessagechecker.AvailableValidators;
import com.cloudogu.scm.commitmessagechecker.Context;
import com.cloudogu.scm.commitmessagechecker.Validator;
import com.cloudogu.scm.commitmessagechecker.config.Configuration;
import com.cloudogu.scm.commitmessagechecker.config.ConfigurationProvider;
import com.cloudogu.scm.commitmessagechecker.config.Validation;
import com.google.common.annotations.VisibleForTesting;
import picocli.CommandLine;
import sonia.scm.cli.CliContext;
import sonia.scm.cli.CliResourceBundle;
import sonia.scm.cli.ParentCommand;
import sonia.scm.cli.TemplateRenderer;
import sonia.scm.repository.NamespaceAndName;
import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryManager;
import sonia.scm.repository.cli.RepositoryCommand;

import javax.inject.Inject;
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
    configurationProvider.evaluateConfiguration(repo)
      .ifPresent(
        configuration -> {
          try {
            validate(configuration, repo);
          } catch (InvalidCommitMessageException e) {
            templateRenderer.renderDefaultError(e);
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
