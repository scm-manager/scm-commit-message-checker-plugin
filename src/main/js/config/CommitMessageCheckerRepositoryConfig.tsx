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
import { useTranslation } from "react-i18next";
import { Configuration } from "@scm-manager/ui-components";
import { Subtitle, useDocumentTitleForRepository } from "@scm-manager/ui-core";
import { Repository } from "@scm-manager/ui-types";
import CommitMessageCheckerValidationEditor from "./CommitMessageCheckerValidationEditor";
import { CommitMessageCheckerConfiguration } from "../types";

type Props = {
  link: string;
  initialConfiguration: CommitMessageCheckerConfiguration;
  onConfigurationChange: (config: CommitMessageCheckerConfiguration, valid: boolean) => void;
  repository: Repository;
};

const CommitMessageCheckerRepositoryConfig: FC<Props> = ({ link, repository }) => {
  const [t] = useTranslation("plugins");
  useDocumentTitleForRepository(repository, t("scm-commit-message-checker-plugin.config.title"));

  return (
    <>
      <Subtitle subtitle={t("scm-commit-message-checker-plugin.config.title")} />
      <Configuration
        link={link}
        render={(props) => <CommitMessageCheckerValidationEditor {...props} global={false} />}
      />
    </>
  );
};

export default CommitMessageCheckerRepositoryConfig;
