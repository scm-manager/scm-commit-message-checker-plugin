import React, { FC } from "react";
import { extensionPoints } from "@scm-manager/ui-extensions";
import { useTranslation } from "react-i18next";

const HgHook: FC<extensionPoints.RepositoryDetailsInformation["props"]> = ({ repository }) => {
  const [t] = useTranslation("plugins");
  return (
    <div className="content">
      <h3 className="is-size-5">{t("scm-commit-message-checker-plugin.hook.hg.title")}</h3>
      <span>{"### " + t("scm-commit-message-checker-plugin.hook.hg.createHook") + "\n\n"}</span>
      <pre>
        <code>{"cd .hg\ntouch validate-commit-message.py\nchmod +x validate-commit-message.py\n\n"}</code>
      </pre>
      <span> {"### " + t("scm-commit-message-checker-plugin.hook.hg.script") + "\n\n"}</span>
      <pre>
        <code>
          {"### " + t("scm-commit-message-checker-plugin.hook.hg.prerequisites") + "\n\n"}
          {"import re,os,sys,mercurial,subprocess\n" +
            "def validate_commit_message(repo, **kwargs):\n" +
            " commitctx = repo.commitctx\n\n" +
            " def commit_ctx(ctx, error):\n" +
            "   branch_name = ctx.branch()\n" +
            "   commit_message = ctx._text\n" +
            `   validation = subprocess.run(['scm', 'repo', 'commit-message-check', ${repository.namespace}/${repository.name}, branch_name, commit_message])\n` +
            "   if validation.returncode > 0:\n" +
            "     sys.exit(validation.returncode)\n" +
            "   return commitctx(ctx, error)\n\n" +
            " repo.commitctx = commit_ctx\n"}
        </code>
      </pre>
      <span>{"### " + t("scm-commit-message-checker-plugin.hook.hg.enableHook") + "\n\n"}</span>
      <pre>
        <code>
          {
            "### Add your hook to your hgrc file\n[hooks]\nprecommit = python:.hg/validate-commit-message.py:validate_commit_message\n\n"
          }
        </code>
      </pre>
    </div>
  );
};

export default HgHook;
