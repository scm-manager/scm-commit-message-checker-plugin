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
import { Icon } from "@scm-manager/ui-components";
import styled from "styled-components";
import { CommitMessageCheckerConfiguration, Validation } from "../types";

type Props = {
  configuration: CommitMessageCheckerConfiguration;
  deleteValidation: (validation: Validation) => void;
  readOnly: boolean;
};

const VCenteredTd = styled.td`
  display: table-cell;
  vertical-align: middle !important;
`;

const NoBorderLeft = styled.table`
  & td:first-child {
    border-left: none;
  }
`;

const ValidationConfigTable: FC<Props> = ({ configuration, deleteValidation, readOnly }) => {
  const [t] = useTranslation("plugins");

  return (
    <NoBorderLeft className="card-table table is-hoverable is-fullwidth">
      <thead>
        <tr>
          <th>{t("scm-commit-message-checker-plugin.config.validationTable.column.name")}</th>
          <th>{t("scm-commit-message-checker-plugin.config.validationTable.column.branches")}</th>
          <th>{t("scm-commit-message-checker-plugin.config.validationTable.column.description")}</th>
          <th>{t("scm-commit-message-checker-plugin.config.validationTable.column.errorMessage")}</th>
          <td className="has-no-style" />
        </tr>
      </thead>
      <tbody>
        {configuration.validations?.map(validation => (
          <tr>
            <td>
              <strong>{t(`validation.${validation.name}.name`)}</strong>
            </td>
            <td>
              {validation?.configuration?.branches
                ? t(`validation.${validation.name}.branches`, { ...validation.configuration })
                : t(`validation.${validation.name}.allBranches`)}
            </td>
            <td>{t(`validation.${validation.name}.description`, { ...validation.configuration })}</td>
            <td>
              {validation?.configuration?.errorMessage
                ? t(`validation.${validation.name}.errorMessage`, { ...validation.configuration })
                : t(`validation.${validation.name}.defaultErrorMessage`)}
            </td>
            {!readOnly ? (
              <VCenteredTd>
                <a
                  className="level-item"
                  onClick={() => deleteValidation(validation)}
                  title={t("scm-commit-message-checker-plugin.config.validationTable.column.deleteValidation")}
                >
                  <span className="icon is-small">
                    <Icon name="trash" color="inherit" />
                  </span>
                </a>
              </VCenteredTd>
            ) : null}
          </tr>
        ))}
      </tbody>
    </NoBorderLeft>
  );
};

export default ValidationConfigTable;
