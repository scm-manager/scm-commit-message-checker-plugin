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

package com.cloudogu.scm.commitmessagechecker;

import org.apache.shiro.authz.AuthorizationException;
import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryPermissions;

public class CommitMessageCheckerPermissions {
  public static boolean mayRead(Repository repository) {
    return RepositoryPermissions.custom(Constants.READ_COMMIT_MESSAGE_CHECKER_PERMISSION, repository).isPermitted() ||
      RepositoryPermissions.custom(Constants.WRITE_COMMIT_MESSAGE_CHECKER_PERMISSION, repository).isPermitted();
  }

  public static void checkRead(Repository repository) {
    if (!(CommitMessageCheckerPermissions.mayRead(repository))) {
      throw new AuthorizationException();
    }
  }
}
