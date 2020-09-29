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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.repository.Repository;

import javax.inject.Inject;
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
      return Optional.of(repoConfig);
    } else if (globalConfiguration.isEnabled()) {
      return Optional.of(globalConfiguration);
    } else {
      LOG.debug("No configuration for commit-message-checker exist.");
      return Optional.empty();
    }
  }
}
