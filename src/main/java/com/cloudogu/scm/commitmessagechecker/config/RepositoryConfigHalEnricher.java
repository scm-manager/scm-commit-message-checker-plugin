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
import sonia.scm.api.v2.resources.Enrich;
import sonia.scm.api.v2.resources.HalAppender;
import sonia.scm.api.v2.resources.HalEnricher;
import sonia.scm.api.v2.resources.HalEnricherContext;
import sonia.scm.api.v2.resources.LinkBuilder;
import sonia.scm.api.v2.resources.ScmPathInfoStore;
import sonia.scm.plugin.Extension;
import sonia.scm.repository.Repository;

import jakarta.inject.Inject;
import jakarta.inject.Provider;

@Extension
@Enrich(Repository.class)
public class RepositoryConfigHalEnricher implements HalEnricher {

  private final Provider<ScmPathInfoStore> scmPathInfoStoreProvider;
  private final ConfigStore configStore;

  @Inject
  public RepositoryConfigHalEnricher(Provider<ScmPathInfoStore> scmPathInfoStoreProvider, ConfigStore configStore) {
    this.scmPathInfoStoreProvider = scmPathInfoStoreProvider;
    this.configStore = configStore;
  }

  @Override
  public void enrich(HalEnricherContext context, HalAppender appender) {
    Repository repository = context.oneRequireByType(Repository.class);
    if (shouldAppendLink(repository)) {
      String linkBuilder = new LinkBuilder(scmPathInfoStoreProvider.get().get(), ConfigurationResource.class)
        .method("getConfiguration")
        .parameters(repository.getNamespace(), repository.getName())
        .href();

      appender.appendLink("commitMessageCheckerConfig", linkBuilder);
    }
  }

  private boolean shouldAppendLink(Repository repository) {
    return !configStore.getGlobalConfiguration().isDisableRepositoryConfiguration()
      && CommitMessageCheckerPermissions.mayRead(repository);
  }

}

