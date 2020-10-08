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
