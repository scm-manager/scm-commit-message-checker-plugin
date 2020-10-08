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

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryTestData;

import java.util.Optional;

import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ConfigurationProviderTest {

  private static final Repository REPOSITORY = RepositoryTestData.createHeartOfGold();
  @Mock
  ConfigStore configStore;

  @InjectMocks
  ConfigurationProvider configurationProvider;

  @Test
  void shouldReturnRepoConfig() {
    mockConfigs(true, false);
    Optional<Configuration> configuration = configurationProvider.evaluateConfiguration(REPOSITORY);

    assertThat(configuration).isPresent();
    assertThat(configuration.get()).isInstanceOf(Configuration.class);
  }

  @Test
  void shouldReturnGlobalConfig() {
    mockConfigs(true, true);
    Optional<Configuration> configuration = configurationProvider.evaluateConfiguration(REPOSITORY);

    assertThat(configuration).isPresent();
    assertThat(configuration.get()).isInstanceOf(GlobalConfiguration.class);
  }

  @Test
  void shouldReturnEmptyOptionalIfBothNotEnabled() {
    mockConfigs(false, false);
    Optional<Configuration> configuration = configurationProvider.evaluateConfiguration(REPOSITORY);

    assertThat(configuration).isNotPresent();
  }

  private void mockConfigs(boolean configEnabled, boolean repoConfigDisabled) {
    when(configStore.getGlobalConfiguration()).thenReturn(new GlobalConfiguration(configEnabled, emptyList(), repoConfigDisabled));
    when(configStore.getConfiguration(REPOSITORY)).thenReturn(new Configuration(configEnabled, emptyList()));
  }

}
