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
