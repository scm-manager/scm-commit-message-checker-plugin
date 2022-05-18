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
import { extensionPoints } from "@scm-manager/ui-extensions";
import { useTranslation } from "react-i18next";

const GitHook: FC<extensionPoints.RepositoryDetailsInformation["props"]> = ({ repository }) => {
  const [t] = useTranslation("plugins");
  return (
    <div className="content">
      <h3 className="is-size-5">{t("scm-commit-message-checker-plugin.hook.git.title")}</h3>
      <span>{"### " + t("scm-commit-message-checker-plugin.hook.git.createHook") + "\n\n"}</span>
      <pre>
        <code>{"cd .git/hooks\ntouch commit-msg\nchmod +x commit-msg\n\n"}</code>
      </pre>
      <span> {"### " + t("scm-commit-message-checker-plugin.hook.git.script") + "\n\n"}</span>
      <pre>
        <code>
          {"#!/bin/bash\n\n"}
          {"### " + t("scm-commit-message-checker-plugin.hook.git.prerequisites") + "\n\n"}
          {"BRANCH_NAME=$(git symbolic-ref --short HEAD)\nCOMMIT_MSG_FILE=`cat $1`\n\n"}
          {`scm repo commit-message-check ${repository.namespace}/${repository.name} $BRANCH_NAME "$COMMIT_MSG_FILE"`}
        </code>
      </pre>
    </div>
  );
};

export default GitHook;
