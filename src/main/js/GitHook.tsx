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

import React, { FC } from "react";
import { Trans, useTranslation } from "react-i18next";
import { extensionPoints } from "@scm-manager/ui-extensions";
import { PreformattedCodeBlock, SubSubtitle } from "@scm-manager/ui-components";

const GitHook: FC<extensionPoints.RepositoryDetailsInformation["props"]> = ({ repository }) => {
  const [t] = useTranslation("plugins");

  const gitCreateHookCommand = "touch .git/hooks/commit-msg\nchmod +x .git/hooks/commit-msg";
  const gitScriptCommand =
    "#!/bin/bash\n\n" +
    t("scm-commit-message-checker-plugin.hook.git.prerequisites") +
    "\n\n" +
    "BRANCH_NAME=$(git symbolic-ref --short HEAD)\n" +
    "COMMIT_MSG_FILE=`cat $1`\n\n" +
    `scm repo commit-message-check ${repository.namespace}/${repository.name} $BRANCH_NAME "$COMMIT_MSG_FILE"`;

  return (
    <div className="content">
      <SubSubtitle>{t("scm-commit-message-checker-plugin.hook.git.title")}</SubSubtitle>
      <p>
        <Trans
          t={t}
          i18nKey="scm-commit-message-checker-plugin.hook.introduction"
          components={[<a href="https://scm-manager.org/cli/">SCM CLI Client</a>]}
        />
      </p>
      <p>
        {t("scm-commit-message-checker-plugin.hook.git.createHook")}
        <PreformattedCodeBlock>{gitCreateHookCommand}</PreformattedCodeBlock>
      </p>
      <p>
        {t("scm-commit-message-checker-plugin.hook.git.script")}
        <PreformattedCodeBlock>{gitScriptCommand}</PreformattedCodeBlock>
      </p>
    </div>
  );
};

export default GitHook;
