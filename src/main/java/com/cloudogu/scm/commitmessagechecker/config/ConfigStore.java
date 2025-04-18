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

import com.cloudogu.scm.commitmessagechecker.AvailableValidators;
import com.cloudogu.scm.commitmessagechecker.Validator;
import lombok.Getter;
import sonia.scm.plugin.PluginLoader;
import sonia.scm.repository.Repository;
import sonia.scm.store.ConfigurationStore;
import sonia.scm.store.ConfigurationStoreFactory;

import jakarta.inject.Inject;
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static com.cloudogu.scm.commitmessagechecker.Constants.NAME;

public class ConfigStore {

  private final AvailableValidators availableValidators;
  private final ClassLoader uberClassLoader;
  private final ConfigurationStoreFactory storeFactory;

  @Inject
  ConfigStore(AvailableValidators availableValidators, ConfigurationStoreFactory storeFactory, PluginLoader pluginLoader) {
    this.availableValidators = availableValidators;
    this.uberClassLoader = pluginLoader.getUberClassLoader();
    this.storeFactory = storeFactory;
  }

  public GlobalConfiguration getGlobalConfiguration() {
    return withUberClassLoader(() -> createGlobalStore().getOptional().orElse(new GlobalConfiguration()));
  }

  public void setGlobalConfiguration(GlobalConfiguration configuration) {
    createGlobalStore().set(configuration);
  }

  public List<ValidatorInstance> getGlobalValidations() {
    return getValidatorInstancesFromConfig(getGlobalConfiguration());
  }

  private ConfigurationStore<GlobalConfiguration> createGlobalStore() {
    return storeFactory
      .withType(GlobalConfiguration.class)
      .withName(NAME)
      .build();
  }

  public Configuration getConfiguration(Repository repository) {
    return withUberClassLoader(() -> createStore(repository).getOptional().orElse(new Configuration()));
  }

  public void setConfiguration(Repository repository, Configuration configuration) {
    createStore(repository).set(configuration);
  }

  public List<ValidatorInstance> getValidations(Repository repository) {
    return getValidatorInstancesFromConfig(getConfiguration(repository));
  }

  private ConfigurationStore<Configuration> createStore(Repository repository) {
    return storeFactory
      .withType(Configuration.class)
      .withName(NAME)
      .forRepository(repository)
      .build();
  }

  private <T> T withUberClassLoader(Supplier<T> runnable) {
    ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
    Thread.currentThread().setContextClassLoader(uberClassLoader);
    try {
      return runnable.get();
    } finally {
      Thread.currentThread().setContextClassLoader(contextClassLoader);
    }
  }

  private List<ValidatorInstance> getValidatorInstancesFromConfig(Configuration configuration) {
    if (!configuration.isEnabled()) {
      return Collections.emptyList();
    }
    return configuration.getValidations()
      .stream()
      .map(this::createValidatorInstance)
      .collect(Collectors.toList());
  }

  private ValidatorInstance createValidatorInstance(Validation validation) {
    Validator validator = availableValidators.validatorFor(validation.getName());
    Object configuration = validation.getConfiguration();
    return new ValidatorInstance(validator, configuration);
  }

  @Getter
  static class ValidatorInstance {
    private final Validator validator;
    private final Object configuration;

    ValidatorInstance(Validator validator, Object configuration) {
      this.validator = validator;
      this.configuration = configuration;
    }
  }
}
