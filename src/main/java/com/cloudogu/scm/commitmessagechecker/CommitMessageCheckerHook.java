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

import javax.inject.Inject;
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
          validate(context, event.getRepository(), validations);
        });
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
