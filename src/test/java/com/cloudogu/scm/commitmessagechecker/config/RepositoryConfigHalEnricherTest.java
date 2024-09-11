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

package com.cloudogu.scm.commitmessagechecker.config;

import com.google.inject.Provider;
import com.google.inject.util.Providers;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.util.ThreadContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.api.v2.resources.HalAppender;
import sonia.scm.api.v2.resources.HalEnricherContext;
import sonia.scm.api.v2.resources.ScmPathInfoStore;
import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryTestData;

import java.net.URI;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RepositoryConfigHalEnricherTest {

  private static final Repository REPOSITORY = RepositoryTestData.createHeartOfGold();

  Provider<ScmPathInfoStore> scmPathInfoStoreProvider;
  @Mock
  ConfigStore configStore;
  @Mock
  HalAppender appender;
  @Mock
  Subject subject;

  RepositoryConfigHalEnricher enricher;

  @BeforeEach
  void setUp() {
    ThreadContext.bind(subject);

    ScmPathInfoStore scmPathInfoStore = new ScmPathInfoStore();
    scmPathInfoStore.set(() -> URI.create("https://scm-manager.org/scm/api/"));
    scmPathInfoStoreProvider = Providers.of(scmPathInfoStore);

    enricher = new RepositoryConfigHalEnricher(scmPathInfoStoreProvider, configStore);
  }

  @AfterEach
  void tearDown() {
    ThreadContext.unbindSubject();
  }

  @Test
  void shouldNotAppendConfigLinkIfRepoConfigDisabled() {
    mockGlobalConfig(true);
    HalEnricherContext context = HalEnricherContext.of(REPOSITORY);

    enricher.enrich(context, appender);

    verify(appender, never()).appendLink(anyString(), anyString());
  }

  @Test
  void shouldNotAppendConfigLinkIfUserNotPermitted() {
    mockGlobalConfig(false);
    when(subject.isPermitted("repository:readCommitMessageCheckerConfig:" + REPOSITORY.getId())).thenReturn(false);
    HalEnricherContext context = HalEnricherContext.of(REPOSITORY);

    enricher.enrich(context, appender);

    verify(appender, never()).appendLink(anyString(), anyString());
  }

  @Test
  void shouldAppendConfigLinkForWritePermission() {
    mockGlobalConfig(false);
    when(subject.isPermitted("repository:readCommitMessageCheckerConfig:" + REPOSITORY.getId())).thenReturn(false);
    when(subject.isPermitted("repository:writeCommitMessageCheckerConfig:" + REPOSITORY.getId())).thenReturn(true);
    HalEnricherContext context = HalEnricherContext.of(REPOSITORY);

    enricher.enrich(context, appender);

    verify(appender).appendLink("commitMessageCheckerConfig", "https://scm-manager.org/scm/api/v2/commit-message-checker/configuration/hitchhiker/HeartOfGold");
  }

  @Test
  void shouldAppendConfigLinkForReadPermission() {
    mockGlobalConfig(false);
    when(subject.isPermitted("repository:readCommitMessageCheckerConfig:" + REPOSITORY.getId())).thenReturn(true);
    HalEnricherContext context = HalEnricherContext.of(REPOSITORY);

    enricher.enrich(context, appender);

    verify(appender).appendLink("commitMessageCheckerConfig", "https://scm-manager.org/scm/api/v2/commit-message-checker/configuration/hitchhiker/HeartOfGold");
  }

  private void mockGlobalConfig(boolean disableRepoConfig) {
    GlobalConfiguration globalConfiguration = new GlobalConfiguration();
    globalConfiguration.setDisableRepositoryConfiguration(disableRepoConfig);
    when(configStore.getGlobalConfiguration()).thenReturn(globalConfiguration);
  }
}
