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
import com.cloudogu.scm.commitmessagechecker.InvalidCommitMessageException;
import com.cloudogu.scm.commitmessagechecker.Validator;
import com.cloudogu.scm.commitmessagechecker.config.Configuration;
import com.cloudogu.scm.commitmessagechecker.config.ConfigurationProvider;
import com.cloudogu.scm.commitmessagechecker.config.Validation;
import com.google.common.collect.ImmutableList;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.cli.CliContext;
import sonia.scm.cli.TemplateRenderer;
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
