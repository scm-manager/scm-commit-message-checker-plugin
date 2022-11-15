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

import com.cloudogu.scm.commitmessagechecker.Constants;
import org.apache.shiro.authz.AuthorizationException;
import sonia.scm.config.ConfigurationPermissions;
import sonia.scm.repository.NamespaceAndName;
import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryManager;
import sonia.scm.repository.RepositoryPermissions;

import javax.inject.Inject;

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
    if (!(RepositoryPermissions.custom(Constants.READ_COMMIT_MESSAGE_CHECKER_PERMISSION, repository).isPermitted() ||
      RepositoryPermissions.custom(Constants.WRITE_COMMIT_MESSAGE_CHECKER_PERMISSION, repository).isPermitted())) {
      throw new AuthorizationException();
    }
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
