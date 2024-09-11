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
