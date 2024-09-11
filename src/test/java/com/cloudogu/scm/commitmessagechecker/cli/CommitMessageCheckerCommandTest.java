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
import com.google.common.collect.ImmutableList;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.cli.CliContext;
import sonia.scm.repository.NamespaceAndName;
import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryManager;
import sonia.scm.repository.RepositoryTestData;

import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CommitMessageCheckerCommandTest {

  private final Repository repository = RepositoryTestData.create42Puzzle();

  @Mock
  private CommitMessageCheckerTemplateRenderer templateRenderer;
  @Mock
  private CliContext context;
  @Mock
  private ConfigurationProvider configurationProvider;
  @Mock
  private AvailableValidators availableValidators;
  @Mock
  private RepositoryManager repositoryManager;

  @InjectMocks
  private CommitMessageCheckerCommand command;

  @Test
  void shouldHandleNotExistingRepository() {
    command.setRepository("no_such/repo");
    when(repositoryManager.get(any(NamespaceAndName.class))).thenReturn(null);

    command.setCommitMessage("My first valid commit");
    command.run();

    verify(templateRenderer).renderNotFoundError();
    verify(configurationProvider, never()).evaluateConfiguration(repository);
  }

  @Nested
  class WithRepository {
    @BeforeEach
    void initCommand() {
      command.setRepository(repository.getNamespaceAndName().toString());
      when(repositoryManager.get(any(NamespaceAndName.class))).thenReturn(repository);
    }

    @Test
    void shouldNotValidateWithoutConfiguredRules() {
      command.setCommitMessage("My first valid commit");
      command.run();

      verify(configurationProvider).evaluateConfiguration(repository);
    }

    @Test
    void shouldValidateConfiguredRules() {
      Validator validator = mock(Validator.class);
      when(availableValidators.validatorFor("CustomRegExValidator")).thenReturn(validator);
      Validation validation = new Validation("CustomRegExValidator", new Object());
      Configuration configuration = new Configuration(true, ImmutableList.of(validation));
      when(configurationProvider.evaluateConfiguration(repository)).thenReturn(Optional.of(configuration));

      command.setCommitMessage("My first valid commit");
      command.run();

      verify(configurationProvider).evaluateConfiguration(repository);
      verify(validator).validate(any(Context.class), eq("My first valid commit"));
    }

    @Test
    void shouldRenderErrorIfNotValid() {
      Validator validator = mock(Validator.class);
      when(availableValidators.validatorFor("CustomRegExValidator")).thenReturn(validator);
      Validation validation = new Validation("CustomRegExValidator", new Object());
      Configuration configuration = new Configuration(true, ImmutableList.of(validation));
      when(configurationProvider.evaluateConfiguration(repository)).thenReturn(Optional.of(configuration));
      when(context.getStderr()).thenReturn(new PrintWriter(new ByteArrayOutputStream()));
      command.setCommitMessage("My first valid commit");

      doThrow(InvalidCommitMessageException.class).when(validator).validate(any(), any());

      command.run();

      verify(templateRenderer).renderDefaultError(any(Exception.class));
      verify(context).exit(2);
    }
  }
}
