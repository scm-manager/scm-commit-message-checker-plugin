import React, { FC } from "react";
import { extensionPoints } from "@scm-manager/ui-extensions";
import { useTranslation } from "react-i18next";

const GitHook: FC<extensionPoints.RepositoryDetailsInformation["props"]> = ({ repository }) => {
  const [t] = useTranslation("plugins");
  return (
    <div className="content">
      <h3 className="is-size-5">{t("scm-commit-message-checker-plugin.hook.git.title")}</h3>
      <pre>
        <code>
          {"#!/bin/bash\n\nBRANCH_NAME=$(git symbolic-ref --short HEAD)\nCOMMIT_MSG_FILE=$1\n\n"}
          {`scm repo commit-message-check ${repository.namespace}/${repository.name} $BRANCH_NAME $COMMIT_MSG_FILE`}
        </code>
      </pre>
    </div>
  );
};

export default GitHook;
