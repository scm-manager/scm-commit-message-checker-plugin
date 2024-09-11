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
