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

import { ConfigurationBinder as cfgBinder } from "@scm-manager/ui-components";
import CommitMessageCheckerGlobalConfig from "./config/CommitMessageCheckerGlobalConfig";
import CommitMessageCheckerRepositoryConfig from "./config/CommitMessageCheckerRepositoryConfig";
import CustomRegExValidatorConfig from "./CustomRegExValidatorConfig";
import { binder, extensionPoints } from "@scm-manager/ui-extensions";
import GitHook from "./GitHook";
import HgHook from "./HgHook";

cfgBinder.bindRepositorySetting(
  "/commit-message-checker",
  "scm-commit-message-checker-plugin.config.link",
  "commitMessageCheckerConfig",
  CommitMessageCheckerRepositoryConfig
);

cfgBinder.bindGlobal(
  "/commit-message-checker",
  "scm-commit-message-checker-plugin.config.link",
  "commitMessageCheckerConfig",
  CommitMessageCheckerGlobalConfig
);

binder.bind("commitMessageChecker.validator.CustomRegExValidator", CustomRegExValidatorConfig);

export const gitPredicate = (props: extensionPoints.RepositoryDetailsInformation["props"]) => {
  return !!(props && props.repository && props.repository.type === "git");
};

binder.bind<extensionPoints.RepositoryDetailsInformation>("repos.repository-details.information", GitHook, {
  predicate: gitPredicate,
  priority: 30
});

export const hgPredicate = (props: extensionPoints.RepositoryDetailsInformation["props"]) => {
  return !!(props && props.repository && props.repository.type === "hg");
};

binder.bind<extensionPoints.RepositoryDetailsInformation>("repos.repository-details.information", HgHook, {
  predicate: hgPredicate,
  priority: 30
});
