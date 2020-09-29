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

import com.cloudogu.scm.commitmessagechecker.AvailableValidators;
import com.cloudogu.scm.commitmessagechecker.Validator;
import lombok.Getter;
import sonia.scm.plugin.PluginLoader;
import sonia.scm.repository.Repository;
import sonia.scm.store.ConfigurationStore;
import sonia.scm.store.ConfigurationStoreFactory;

import javax.inject.Inject;
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
    Validator validator = availableValidators.validatorOf(validation.getName());
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
