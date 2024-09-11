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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.repository.Repository;

import jakarta.inject.Inject;
import java.util.Optional;

public class ConfigurationProvider {

  private static final Logger LOG = LoggerFactory.getLogger(ConfigurationProvider.class);

  private final ConfigStore configStore;

  @Inject
  public ConfigurationProvider(ConfigStore configStore) {
    this.configStore = configStore;
  }

  public Optional<Configuration> evaluateConfiguration(Repository repository) {
    GlobalConfiguration globalConfiguration = configStore.getGlobalConfiguration();
    Configuration repoConfig = configStore.getConfiguration(repository);

    if (!globalConfiguration.isDisableRepositoryConfiguration() && repoConfig.isEnabled()) {
      LOG.debug("Using commit-message-checker configuration from repository: {}", repository.getNamespaceAndName());
      return Optional.of(repoConfig);
    } else if (globalConfiguration.isEnabled()) {
      LOG.debug("Using global commit-message-checker configuration.");
      return Optional.of(globalConfiguration);
    } else {
      LOG.debug("No configuration for commit-message-checker exist.");
      return Optional.empty();
    }
  }
}
