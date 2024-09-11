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

import com.cloudogu.scm.commitmessagechecker.config.Configuration;
import com.cloudogu.scm.commitmessagechecker.config.ConfigurationProvider;
import com.cloudogu.scm.commitmessagechecker.config.Validation;
import com.google.common.collect.ImmutableList;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.repository.Changeset;
import sonia.scm.repository.PreReceiveRepositoryHookEvent;
import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryHookEvent;
import sonia.scm.repository.RepositoryHookType;
import sonia.scm.repository.RepositoryTestData;
import sonia.scm.repository.api.HookChangesetBuilder;
import sonia.scm.repository.api.HookContext;
import sonia.scm.repository.api.HookFeature;
import sonia.scm.repository.api.HookMessageProvider;

import java.util.Optional;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CommitMessageCheckerHookTest {

  private static final Repository REPOSITORY = RepositoryTestData.createHeartOfGold();

  @Mock
  private ConfigurationProvider configurationProvider;
  @Mock
  private AvailableValidators availableValidators;
  @Mock
  private HookContext hookContext;
  @Mock
  private HookChangesetBuilder hookChangesetBuilder;
  @Mock
  private Validator validator;
  @Captor
  private ArgumentCaptor<Context> contextCaptor;

  @InjectMocks
  private CommitMessageCheckerHook hook;

  @Test
  void shouldNotValidateIfNoValidationsFound() {
    when(configurationProvider.evaluateConfiguration(REPOSITORY))
      .thenReturn(Optional.of(new Configuration(true, emptyList())));

    PreReceiveRepositoryHookEvent event = new PreReceiveRepositoryHookEvent(new RepositoryHookEvent(hookContext, REPOSITORY, RepositoryHookType.PRE_RECEIVE));
    hook.onEvent(event);

    verify(availableValidators, never()).validatorFor(anyString());
  }

  @Test
  void shouldNotValidateIfConfigurationIsDisabled() {
    when(configurationProvider.evaluateConfiguration(REPOSITORY)).thenReturn(Optional.of(new Configuration(false, emptyList())));

    PreReceiveRepositoryHookEvent event = new PreReceiveRepositoryHookEvent(new RepositoryHookEvent(hookContext, REPOSITORY, RepositoryHookType.PRE_RECEIVE));
    hook.onEvent(event);

    verify(availableValidators, never()).validatorFor(anyString());
  }

  @Test
  void shouldValidate() {
    Object configuration = prepareValidator("master");

    PreReceiveRepositoryHookEvent event = new PreReceiveRepositoryHookEvent(new RepositoryHookEvent(hookContext, REPOSITORY, RepositoryHookType.PRE_RECEIVE));
    hook.onEvent(event);

    verify(validator).validate(contextCaptor.capture(), eq("awesome commit"));

    Context context = contextCaptor.getValue();
    assertThat(context.getBranch()).isEqualTo("master");
    assertThat(context.getRepository()).isEqualTo(REPOSITORY);
    assertThat(context.getConfiguration()).isEqualTo(configuration);
  }

  @Test
  void shouldValidateChangesetWithoutBranches() {
    Object configuration = prepareValidator();

    PreReceiveRepositoryHookEvent event = new PreReceiveRepositoryHookEvent(new RepositoryHookEvent(hookContext, REPOSITORY, RepositoryHookType.PRE_RECEIVE));
    hook.onEvent(event);

    verify(validator).validate(contextCaptor.capture(), eq("awesome commit"));

    Context context = contextCaptor.getValue();
    assertThat(context.getBranch()).isEmpty();
    assertThat(context.getRepository()).isEqualTo(REPOSITORY);
    assertThat(context.getConfiguration()).isEqualTo(configuration);
  }

  @Test
  void shouldSendContextMessage() {
    when(hookContext.isFeatureSupported(HookFeature.MESSAGE_PROVIDER)).thenReturn(true);
    HookMessageProvider messageProvider = mock(HookMessageProvider.class);
    when(hookContext.getMessageProvider()).thenReturn(messageProvider);

    prepareValidator("master");
    doThrow(RuntimeException.class).when(validator).validate(contextCaptor.capture(), eq("awesome commit"));

    PreReceiveRepositoryHookEvent event = new PreReceiveRepositoryHookEvent(new RepositoryHookEvent(hookContext, REPOSITORY, RepositoryHookType.PRE_RECEIVE));
    assertThrows(RuntimeException.class, () -> hook.onEvent(event));

    verify(messageProvider).sendError("Commit message validation:");
  }

  private Object prepareValidator(String... branches) {
    Object configuration = new Object();
    when(configurationProvider.evaluateConfiguration(REPOSITORY))
      .thenReturn(Optional.of(new Configuration(true, ImmutableList.of(new Validation("TestValidator", configuration)))));
    when(hookContext.getChangesetProvider()).thenReturn(hookChangesetBuilder);
    Changeset changeset = new Changeset("1", 1L, null, "awesome commit");
    when(hookChangesetBuilder.getChangesetList()).thenReturn(ImmutableList.of(changeset));
    when(availableValidators.validatorFor("TestValidator")).thenReturn(validator);
    changeset.setBranches(asList(branches));
    return configuration;
  }
}
