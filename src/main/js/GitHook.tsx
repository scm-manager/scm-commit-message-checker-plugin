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
