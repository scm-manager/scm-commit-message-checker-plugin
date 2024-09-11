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

import React, { FC, useEffect, useState } from "react";
import { useTranslation } from "react-i18next";
import { InputField } from "@scm-manager/ui-components";

type Configuration = {
  pattern?: string;
  branches?: string;
  errorMessage?: string;
};

type ConfigProps = {
  configurationChanged: (newRuleConfiguration: Configuration, valid: boolean) => void;
};

const CustomRegExValidatorConfig: FC<ConfigProps> = ({ configurationChanged }) => {
  const [t] = useTranslation("plugins");
  const [pattern, setPattern] = useState<string | undefined>();
  const [branches, setBranches] = useState<string | undefined>();
  const [errorMessage, setErrorMessage] = useState<string | undefined>();
  const [patternValidationError, setPatternValidationError] = useState(false);

  useEffect(() => configurationChanged({ pattern, branches, errorMessage }, false), []);

  const onPatternChange = (value: string) => {
    setPattern(value);
    if (value?.trim()?.length > 0) {
      setPatternValidationError(false);
      configurationChanged({ pattern: value, errorMessage, branches }, true);
    } else {
      setPatternValidationError(true);
      configurationChanged({ pattern: undefined, errorMessage, branches }, false);
    }
  };

  const onBranchesChange = (value: string) => {
    setBranches(value);
    configurationChanged({ branches: value, errorMessage, pattern }, !!pattern);
  };

  const onErrorMessageChange = (value: string) => {
    setErrorMessage(value);
    configurationChanged({ errorMessage: value, branches, pattern }, !!pattern);
  };

  return (
    <>
      <InputField
        value={pattern}
        label={t("validator.CustomRegExValidator.pattern.label")}
        helpText={t("validator.CustomRegExValidator.pattern.helpText")}
        validationError={patternValidationError}
        errorMessage={t("validator.CustomRegExValidator.pattern.errorMessage")}
        autofocus={true}
        onChange={onPatternChange}
      />
      <InputField
        value={branches}
        label={t("validator.CustomRegExValidator.branches.label")}
        helpText={t("validator.CustomRegExValidator.branches.helpText")}
        onChange={onBranchesChange}
      />
      <InputField
        value={errorMessage}
        label={t("validator.CustomRegExValidator.errorMessage.label")}
        helpText={t("validator.CustomRegExValidator.errorMessage.helpText")}
        onChange={onErrorMessageChange}
      />
    </>
  );
};

export default CustomRegExValidatorConfig;
