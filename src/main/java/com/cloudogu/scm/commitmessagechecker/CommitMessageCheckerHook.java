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

import com.cloudogu.scm.commitmessagechecker.config.ConfigurationProvider;
import com.cloudogu.scm.commitmessagechecker.config.Validation;
import com.github.legman.Subscribe;
import lombok.extern.slf4j.Slf4j;
import sonia.scm.EagerSingleton;
import sonia.scm.plugin.Extension;
import sonia.scm.repository.Changeset;
import sonia.scm.repository.PreReceiveRepositoryHookEvent;
import sonia.scm.repository.Repository;
import sonia.scm.repository.api.HookContext;
import sonia.scm.repository.api.HookFeature;

import jakarta.inject.Inject;
import java.util.List;

@Slf4j
@Extension
@EagerSingleton
public class CommitMessageCheckerHook {

  private final ConfigurationProvider configurationProvider;
  private final AvailableValidators availableValidators;

  @Inject
  public CommitMessageCheckerHook(ConfigurationProvider configurationProvider, AvailableValidators availableValidators) {
    this.configurationProvider = configurationProvider;
    this.availableValidators = availableValidators;
  }

  @Subscribe(async = false)
  public void onEvent(PreReceiveRepositoryHookEvent event) {
    HookContext context = event.getContext();

    configurationProvider.evaluateConfiguration(event.getRepository())
      .ifPresent(
        configuration -> {
          List<Validation> validations = configuration.getValidations();
          try {
            validate(context, event.getRepository(), validations);
          } catch (RuntimeException e) {
            contextualizeError(event);
            throw e;
          }
        });
  }

  private void contextualizeError(PreReceiveRepositoryHookEvent event) {
    if (event.getContext().isFeatureSupported(HookFeature.MESSAGE_PROVIDER)) {
      event.getContext().getMessageProvider().sendError("Commit message validation:");
    }
  }

  private void validate(HookContext context, Repository repository, List<Validation> validations) {
    if (validations.isEmpty()) {
      log.debug("No validations found");
      return;
    }

    for (Validation validation : validations) {
      Validator validator = availableValidators.validatorFor(validation.getName());
      for (Changeset changeset : context.getChangesetProvider().getChangesetList()) {
        if (!changeset.getBranches().isEmpty()) {
          for (String branch : changeset.getBranches()) {
            validator.validate(new Context(repository, branch, validation.getConfiguration()), changeset.getDescription());
          }
        } else {
          validator.validate(new Context(repository, "", validation.getConfiguration()), changeset.getDescription());
        }
      }
    }
  }
}
