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
import { useTranslation } from "react-i18next";
import { Icon } from "@scm-manager/ui-components";
import styled from "styled-components";
import { CommitMessageCheckerConfiguration, Validation } from "../types";

type Props = {
  configuration: CommitMessageCheckerConfiguration;
  deleteValidation: (validation: Validation) => void;
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

const ValidationConfigTable: FC<Props> = ({ configuration, deleteValidation }) => {
  const [t] = useTranslation("plugins");

  return (
    <NoBorderLeft className="card-table table is-hoverable is-fullwidth">
      <thead>
        <tr>
          <th>{t("scm-commit-message-checker-plugin.validation.column.name")}</th>
          <th>{t("scm-commit-message-checker-plugin.validation.column.description")}</th>
          <th></th>
        </tr>
      </thead>
      <tbody>
        {configuration.validations?.map(validation => (
          <tr>
            <td>
              <strong>{t(`validation.${validation.name}.name`)}</strong>
            </td>
            <td>{t(`validation.${validation.name}.description`, validation.configuration)}</td>
            <VCenteredTd>
              <a
                className="level-item"
                onClick={() => deleteValidation(validation)}
                title={t("scm-commit-message-checker-plugin.deleteValidation")}
              >
                <span className="icon is-small">
                  <Icon name="trash" color="inherit" />
                </span>
              </a>
            </VCenteredTd>
          </tr>
        ))}
      </tbody>
    </NoBorderLeft>
  );
};

export default ValidationConfigTable;
