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

const HgHook: FC<extensionPoints.RepositoryDetailsInformation["props"]> = ({ repository }) => {
  const [t] = useTranslation("plugins");

  const hgCreateHookCommand = "touch .hg/validate-commit-message.py\nchmod +x .hg/validate-commit-message.py";
  const hgScriptCommand =
    t("scm-commit-message-checker-plugin.hook.hg.prerequisites") +
    "\n\n" +
    "import re,os,sys,mercurial,subprocess\n" +
    "def validate_commit_message(repo, **kwargs):\n" +
    "  commitctx = repo.commitctx\n" +
    "  def commit_ctx(ctx, error):\n" +
    "    branch_name = ctx.branch()\n" +
    "    commit_message = ctx._text\n" +
    `    exit_code = subprocess.call(['scm', 'repo', 'commit-message-check', '${repository.namespace}/${repository.name}', branch_name, commit_message])\n` +
    "    if exit_code > 0:\n" +
    "      sys.exit(exit_code)\n" +
    "    return commitctx(ctx, error)\n" +
    "  repo.commitctx = commit_ctx";
  const hgEnableHookCommand = "[hooks]\nprecommit = python:.hg/validate-commit-message.py:validate_commit_message";

  return (
    <div className="content">
      <SubSubtitle>{t("scm-commit-message-checker-plugin.hook.hg.title")}</SubSubtitle>
      <p>
        <Trans
          t={t}
          i18nKey="scm-commit-message-checker-plugin.hook.introduction"
          components={[<a href="https://scm-manager.org/cli/">SCM CLI Client</a>]}
        />
      </p>
      <p>
        {t("scm-commit-message-checker-plugin.hook.hg.createHook")}
        <PreformattedCodeBlock>{hgCreateHookCommand}</PreformattedCodeBlock>
      </p>
      <p>
        {t("scm-commit-message-checker-plugin.hook.hg.script")}
        <PreformattedCodeBlock>{hgScriptCommand}</PreformattedCodeBlock>
      </p>
      <p>
        {t("scm-commit-message-checker-plugin.hook.hg.enableHook")}
        <PreformattedCodeBlock>{hgEnableHookCommand}</PreformattedCodeBlock>
      </p>
    </div>
  );
};

export default HgHook;
