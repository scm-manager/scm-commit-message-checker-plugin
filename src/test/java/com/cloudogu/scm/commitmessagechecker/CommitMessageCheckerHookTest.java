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

import java.util.Collections;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
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
  void shouldNotValidateIfConfigurationIsDisabled() {
    when(configurationProvider.evaluateConfiguration(REPOSITORY)).thenReturn(Optional.of(new Configuration(false, Collections.emptyList())));

    PreReceiveRepositoryHookEvent event = new PreReceiveRepositoryHookEvent(new RepositoryHookEvent(hookContext, REPOSITORY, RepositoryHookType.PRE_RECEIVE));
    hook.onEvent(event);

    verify(availableValidators, never()).validatorOf(anyString());
  }

  @Test
  void shouldValidate() {
    Object configuration = new Object();
    when(configurationProvider.evaluateConfiguration(REPOSITORY))
      .thenReturn(Optional.of(new Configuration(true, ImmutableList.of(new Validation("TestValidator", configuration)))));
    when(hookContext.getChangesetProvider()).thenReturn(hookChangesetBuilder);
    Changeset changeset = new Changeset("1", 1L, null, "awesome commit");
    changeset.setBranches(ImmutableList.of("master"));
    when(hookChangesetBuilder.getChangesetList()).thenReturn(ImmutableList.of(changeset));
    when(availableValidators.validatorOf("TestValidator")).thenReturn(validator);

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
    Object configuration = new Object();
    when(configurationProvider.evaluateConfiguration(REPOSITORY))
      .thenReturn(Optional.of(new Configuration(true, ImmutableList.of(new Validation("TestValidator", configuration)))));
    when(hookContext.getChangesetProvider()).thenReturn(hookChangesetBuilder);
    Changeset changeset = new Changeset("1", 1L, null, "awesome commit");
    when(hookChangesetBuilder.getChangesetList()).thenReturn(ImmutableList.of(changeset));
    when(availableValidators.validatorOf("TestValidator")).thenReturn(validator);

    PreReceiveRepositoryHookEvent event = new PreReceiveRepositoryHookEvent(new RepositoryHookEvent(hookContext, REPOSITORY, RepositoryHookType.PRE_RECEIVE));
    hook.onEvent(event);

    verify(validator).validate(contextCaptor.capture(), eq("awesome commit"));

    Context context = contextCaptor.getValue();
    assertThat(context.getBranch()).isEmpty();
    assertThat(context.getRepository()).isEqualTo(REPOSITORY);
    assertThat(context.getConfiguration()).isEqualTo(configuration);
  }
}
