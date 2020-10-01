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
import { AvailableValidators, CommitMessageCheckerConfiguration, Validation, Validator } from "../types";
import {
  AddButton,
  apiClient,
  Checkbox,
  ErrorNotification,
  Level,
  Loading,
  Notification,
  Select
} from "@scm-manager/ui-components";
import { useTranslation } from "react-i18next";
import ValidationConfigTable from "./ValidationConfigTable";
import styled from "styled-components";
import { ExtensionPoint } from "@scm-manager/ui-extensions";
import { Link } from "@scm-manager/ui-types";

type Props = {
  initialConfiguration: CommitMessageCheckerConfiguration;
  onConfigurationChange: (config: CommitMessageCheckerConfiguration, valid: boolean) => void;
  global: boolean;
};

const AddValidatorLevel = styled.div`
  display: flex;
  justify-content: space-between;
  align-items: center;
`;

const ValidatorDetails = styled.p`
  flex: 1;
  padding: 1rem;
  background: #f5f5f5;
  border-radius: 4px;
`;

const CommitMessageCheckerValidationEditor: FC<Props> = ({ initialConfiguration, onConfigurationChange, global }) => {
  const [t] = useTranslation("plugins");
  const [config, setConfig] = useState(initialConfiguration);
  const [availableValidators, setAvailableValidators] = useState<Validator[]>([]);
  const [selectedValidator, setSelectedValidator] = useState("");
  const [validatorConfiguration, setValidatorConfiguration] = useState<any>(undefined);
  const [validatorConfigurationValid, setValidatorConfigurationValid] = useState(true);
  const [error, setError] = useState<Error | undefined>(undefined);
  const [loading, setLoading] = useState(true);

  const availableValidatorsHref = (config._links.availableValidators as Link).href;

  useEffect(() => {
    setLoading(true);
    apiClient
      .get(availableValidatorsHref)
      .then(r => r.json() as Promise<AvailableValidators>)
      .then(body => body.validators)
      .then(setAvailableValidators)
      .then(() => setLoading(false))
      .catch(err => {
        setError(err);
        setLoading(false);
      });
  }, [availableValidatorsHref]);

  useEffect(() => {
    onConfigurationChange(config, true);
  }, [config]);

  const deleteValidation = (validation: Validation) => {
    const newValidations = [...config.validations];
    const index = newValidations.indexOf(validation);
    newValidations.splice(index, 1);
    const newConfig = { ...config, validations: newValidations };
    setConfig(newConfig);
  };

  const selectValidation = (value: string) => {
    setSelectedValidator(value);
    setValidatorConfiguration(null);
    setValidatorConfigurationValid(true);
  };

  const addValidationToConfig = (value: Validation) => {
    if (value) {
      const newConfig = { ...config, validations: [...config.validations, value] };
      setSelectedValidator("");
      setConfig(newConfig);
    }
  };

  const options = [
    { label: "", value: "" },
    ...availableValidators
      .filter(
        availableValidator =>
          availableValidator.applicableMultipleTimes ||
          !config.validations.find(v => v.name === availableValidator.name)
      )
      // .sort((a, b) => bySortKey(a.name, b.name, t.bind(t))) //TODO Do we need a sort?
      .map(validator => ({ label: t(`validator.${validator.name}.name`), value: validator.name }))
  ];

  const renderAddValidationForm = () => {
    if (loading) {
      return <Loading />;
    }

    if (availableValidators?.length > 0) {
      return (
        <Select
          label={t("scm-commit-message-checker-plugin.config.newValidation.label")}
          helpText={t("scm-commit-message-checker-plugin.config.newValidation.helpText")}
          onChange={selectValidation}
          options={options}
          value={selectedValidator}
        />
      );
    }

    return (
      <Notification type={"info"}>
        {t("scm-commit-message-checker-plugin.config.noMoreValidatorsAvailable")}
      </Notification>
    );
  };

  const disableRepositoryConfigurationCheckbox = global && (
    <div className="column is-full">
      <Checkbox
        name={"disableRepositoryConfiguration"}
        label={t("scm-commit-message-checker-plugin.config.disableRepositoryConfiguration.label")}
        helpText={t("scm-commit-message-checker-plugin.config.disableRepositoryConfiguration.helpText")}
        checked={config?.disableRepositoryConfiguration ? config.disableRepositoryConfiguration : false}
        onChange={disableRepositoryConfiguration => setConfig({ ...config, disableRepositoryConfiguration })}
      />
    </div>
  );

  const enabledCheckbox = (
    <div className="column is-full">
      <Checkbox
        name={"enabled"}
        label={t("scm-commit-message-checker-plugin.config.enabled.label")}
        helpText={t("scm-commit-message-checker-plugin.config.enabled.helpText")}
        checked={config.enabled}
        onChange={enabled => setConfig({ ...config, enabled })}
      />
    </div>
  );

  const validationConfigTable =
    config.validations.length > 0 ? (
      <ValidationConfigTable configuration={config} deleteValidation={deleteValidation} />
    ) : (
      <Notification type="info">{t("scm-commit-message-checker-plugin.config.noValidationsConfigured")}</Notification>
    );

  const validatorConfigChanged = (validatorConfig: any, valid: boolean) => {
    setValidatorConfiguration(validatorConfig);
    setValidatorConfigurationValid(valid);
  };

  if (error) {
    return <ErrorNotification error={error} />;
  }

  return (
    <>
      <div className="columns is-multiline">
        {disableRepositoryConfigurationCheckbox}
        {enabledCheckbox}
      </div>
      {config.enabled && (
        <>
          {validationConfigTable}
          {renderAddValidationForm()}
          {selectedValidator && (
            <AddValidatorLevel>
              <ValidatorDetails>
                <h4>{t("validator." + selectedValidator + ".description", validatorConfiguration)}</h4>
                <ExtensionPoint
                  name={`commitMessageChecker.validator.${selectedValidator}`}
                  renderAll={true}
                  props={{
                    configurationChanged: validatorConfigChanged
                  }}
                />
                <Level
                  right={
                    <AddButton
                      label={t("scm-commit-message-checker-plugin.config.addValidation.label")}
                      action={() =>
                        addValidationToConfig({
                          name: selectedValidator,
                          configuration: validatorConfiguration
                        })
                      }
                      disabled={!validatorConfigurationValid}
                    />
                  }
                />
              </ValidatorDetails>
            </AddValidatorLevel>
          )}
        </>
      )}
    </>
  );
};

export default CommitMessageCheckerValidationEditor;
