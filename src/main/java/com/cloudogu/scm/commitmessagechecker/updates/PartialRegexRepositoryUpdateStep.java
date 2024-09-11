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

package com.cloudogu.scm.commitmessagechecker.updates;

import com.cloudogu.scm.commitmessagechecker.updates.PartialRegexUpdater.RepositoryRootConfiguration;
import sonia.scm.migration.RepositoryUpdateContext;
import sonia.scm.migration.RepositoryUpdateStep;
import sonia.scm.plugin.Extension;
import sonia.scm.store.ConfigurationStore;
import sonia.scm.store.ConfigurationStoreFactory;
import sonia.scm.version.Version;

import jakarta.inject.Inject;

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
