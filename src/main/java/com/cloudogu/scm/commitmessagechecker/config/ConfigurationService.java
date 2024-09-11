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

import com.cloudogu.scm.commitmessagechecker.CommitMessageCheckerPermissions;
import com.cloudogu.scm.commitmessagechecker.Constants;
import sonia.scm.config.ConfigurationPermissions;
import sonia.scm.repository.NamespaceAndName;
import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryManager;
import sonia.scm.repository.RepositoryPermissions;

import jakarta.inject.Inject;

import static sonia.scm.ContextEntry.ContextBuilder.entity;
import static sonia.scm.NotFoundException.notFound;

class ConfigurationService {

  private final ConfigStore configStore;
  private final RepositoryManager repositoryManager;
  private final ConfigurationMapper mapper;

  @Inject
  public ConfigurationService(ConfigStore configStore, RepositoryManager repositoryManager, ConfigurationMapper mapper) {
    this.configStore = configStore;
    this.repositoryManager = repositoryManager;
    this.mapper = mapper;
  }

  public ConfigurationDto getRepositoryConfiguration(String namespace, String name) {
    Repository repository = loadRepository(namespace, name);
    CommitMessageCheckerPermissions.checkRead(repository);
    Configuration configuration = configStore.getConfiguration(repository);
    return mapper.map(configuration, repository);
  }

  public void updateRepositoryConfiguration(String namespace, String name, ConfigurationDto updatedConfig) {
    Repository repository = loadRepository(namespace, name);
    RepositoryPermissions.custom(Constants.WRITE_COMMIT_MESSAGE_CHECKER_PERMISSION, repository).check();
    configStore.setConfiguration(repository, mapper.map(updatedConfig));
  }

  public GlobalConfigurationDto getGlobalConfiguration() {
    ConfigurationPermissions.read(Constants.NAME).check();
    GlobalConfiguration globalConfiguration = configStore.getGlobalConfiguration();
    return mapper.map(globalConfiguration);
  }

  public void updateGlobalConfiguration(GlobalConfigurationDto updatedGlobalConfiguration) {
    ConfigurationPermissions.write(Constants.NAME).check();
    configStore.setGlobalConfiguration(mapper.map(updatedGlobalConfiguration));
  }

  private Repository loadRepository(String namespace, String name) {
    Repository repository = repositoryManager.get(new NamespaceAndName(namespace, name));
    if (repository == null) {
      throw notFound(entity(new NamespaceAndName(namespace, name)));
    }
    return repository;
  }
}
