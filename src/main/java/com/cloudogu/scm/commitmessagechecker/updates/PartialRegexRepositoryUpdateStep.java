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

package com.cloudogu.scm.commitmessagechecker.updates;

import com.cloudogu.scm.commitmessagechecker.updates.PartialRegexUpdater.RepositoryRootConfiguration;
import sonia.scm.migration.RepositoryUpdateContext;
import sonia.scm.migration.RepositoryUpdateStep;
import sonia.scm.plugin.Extension;
import sonia.scm.store.ConfigurationStore;
import sonia.scm.store.ConfigurationStoreFactory;
import sonia.scm.version.Version;

import javax.inject.Inject;

@Extension
public class PartialRegexRepositoryUpdateStep implements RepositoryUpdateStep {

  private final ConfigurationStoreFactory storeFactory;

  @Inject
  public PartialRegexRepositoryUpdateStep(ConfigurationStoreFactory storeFactory) {
    this.storeFactory = storeFactory;
  }

  @Override
  public void doUpdate(RepositoryUpdateContext repositoryUpdateContext) {
    ConfigurationStore<RepositoryRootConfiguration> store = storeFactory
      .withType(RepositoryRootConfiguration.class)
      .withName("commitMessageChecker")
      .forRepository(repositoryUpdateContext.getRepositoryId())
      .build();

    store.getOptional()
      .map(PartialRegexUpdater::doUpdate)
      .ifPresent(store::set);
  }

  @Override
  public Version getTargetVersion() {
    return Version.parse("1.1.0");
  }

  @Override
  public String getAffectedDataType() {
    return "scm.commit-message-checker.regex";
  }
}
