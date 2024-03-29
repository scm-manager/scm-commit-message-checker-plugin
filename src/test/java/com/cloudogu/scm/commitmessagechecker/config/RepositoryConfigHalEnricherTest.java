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
