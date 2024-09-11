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

package com.cloudogu.scm.commitmessagechecker.config;

import com.cloudogu.scm.commitmessagechecker.AvailableValidators;
import com.cloudogu.scm.commitmessagechecker.Context;
import com.cloudogu.scm.commitmessagechecker.Validator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.util.ThreadContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.api.v2.resources.ScmPathInfoStore;
import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryTestData;

import jakarta.validation.ConstraintViolationException;
import java.net.URI;
import java.util.Collections;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ConfigurationMapperTest {

  private static final Repository REPOSITORY = RepositoryTestData.createHeartOfGold();
  @Mock
  private AvailableValidators availableValidators;
  @Mock
  private ConfigurationValidator configurationValidator;
  @Mock
  private ScmPathInfoStore scmPathInfoStore;
  @Mock
  private Subject subject;
  @InjectMocks
  private ConfigurationMapperImpl mapper;

  @BeforeEach
  void setUp() {
    ThreadContext.bind(subject);

    lenient().when(scmPathInfoStore.get()).thenReturn(() -> URI.create("scm/"));
  }

  @AfterEach
  void tearDown() {
    ThreadContext.unbindSubject();
  }

  @Test
  void shouldMapGlobalConfigWithLinks() {
    when(subject.isPermitted("configuration:write:commitMessageChecker")).thenReturn(true);

    GlobalConfiguration globalConfiguration = new GlobalConfiguration(true, Collections.emptyList(), true);
    GlobalConfigurationDto dto = mapper.map(globalConfiguration);

    assertThat(dto.isDisableRepositoryConfiguration()).isTrue();
    assertThat(dto.isEnabled()).isTrue();
    assertThat(dto.getValidations()).isEmpty();
    assertThat(dto.getLinks().getLinkBy("self").get().getHref()).isEqualTo("scm/v2/commit-message-checker/configuration/");
    assertThat(dto.getLinks().getLinkBy("update").get().getHref()).isEqualTo("scm/v2/commit-message-checker/configuration/");
    assertThat(dto.getLinks().getLinkBy("availableValidators").get().getHref()).isEqualTo("scm/v2/commit-message-checker/configuration/validators");
  }

  @Test
  void shouldMapGlobalConfigWithoutUpdateLink() {
    when(subject.isPermitted("configuration:write:commitMessageChecker")).thenReturn(false);
    when(subject.isPermitted("configuration:read:commitMessageChecker")).thenReturn(true);

    GlobalConfiguration globalConfiguration = new GlobalConfiguration(true, Collections.emptyList(), true);
    GlobalConfigurationDto dto = mapper.map(globalConfiguration);

    assertThat(dto.isDisableRepositoryConfiguration()).isTrue();
    assertThat(dto.isEnabled()).isTrue();
    assertThat(dto.getValidations()).isEmpty();
    assertThat(dto.getLinks().getLinkBy("self").get().getHref()).isEqualTo("scm/v2/commit-message-checker/configuration/");
    assertThat(dto.getLinks().getLinkBy("availableValidators").get().getHref()).isEqualTo("scm/v2/commit-message-checker/configuration/validators");
    assertThat(dto.getLinks().getLinkBy("update")).isNotPresent();
  }

  @Test
  void shouldMapGlobalConfigWithSelfLinkOnly() {
    GlobalConfiguration globalConfiguration = new GlobalConfiguration(true, Collections.emptyList(), true);
    GlobalConfigurationDto dto = mapper.map(globalConfiguration);

    assertThat(dto.isDisableRepositoryConfiguration()).isTrue();
    assertThat(dto.isEnabled()).isTrue();
    assertThat(dto.getValidations()).isEmpty();
    assertThat(dto.getLinks().getLinkBy("self").get().getHref()).isEqualTo("scm/v2/commit-message-checker/configuration/");
    assertThat(dto.getLinks().getLinkBy("availableValidators")).isNotPresent();
    assertThat(dto.getLinks().getLinkBy("update")).isNotPresent();
  }

  @Test
  void shouldMapGlobalConfigDto() {
    GlobalConfigurationDto dto = new GlobalConfigurationDto();
    GlobalConfiguration globalConfiguration = mapper.map(dto);

    assertThat(globalConfiguration.isDisableRepositoryConfiguration()).isFalse();
    assertThat(globalConfiguration.isEnabled()).isFalse();
    assertThat(globalConfiguration.getValidations()).isNullOrEmpty();
  }

  @Test
  void shouldMapRepoConfigWithLinks() {
    when(subject.isPermitted("repository:writeCommitMessageCheckerConfig:" + REPOSITORY.getId())).thenReturn(true);

    Configuration configuration = new Configuration(true, Collections.emptyList());
    ConfigurationDto dto = mapper.map(configuration, REPOSITORY);

    assertThat(dto.isEnabled()).isTrue();
    assertThat(dto.getValidations()).isEmpty();
    assertThat(dto.getLinks().getLinkBy("self").get().getHref()).isEqualTo("scm/v2/commit-message-checker/configuration/" + REPOSITORY.getNamespaceAndName());
    assertThat(dto.getLinks().getLinkBy("update").get().getHref()).isEqualTo("scm/v2/commit-message-checker/configuration/" + REPOSITORY.getNamespaceAndName());
    assertThat(dto.getLinks().getLinkBy("availableValidators").get().getHref()).isEqualTo("scm/v2/commit-message-checker/configuration/validators");
  }

  @Test
  void shouldMapRepoConfigWithoutUpdateLink() {
    when(subject.isPermitted("repository:writeCommitMessageCheckerConfig:" + REPOSITORY.getId())).thenReturn(false);
    when(subject.isPermitted("repository:readCommitMessageCheckerConfig:" + REPOSITORY.getId())).thenReturn(true);

    Configuration configuration = new Configuration(true, Collections.emptyList());
    ConfigurationDto dto = mapper.map(configuration, REPOSITORY);

    assertThat(dto.isEnabled()).isTrue();
    assertThat(dto.getValidations()).isEmpty();
    assertThat(dto.getLinks().getLinkBy("self").get().getHref()).isEqualTo("scm/v2/commit-message-checker/configuration/" + REPOSITORY.getNamespaceAndName());
    assertThat(dto.getLinks().getLinkBy("availableValidators").get().getHref()).isEqualTo("scm/v2/commit-message-checker/configuration/validators");
    assertThat(dto.getLinks().getLinkBy("update")).isNotPresent();
  }

  @Test
  void shouldMapRepoConfigWithSelfLinkOnly() {
    Configuration configuration = new Configuration(true, Collections.emptyList());
    ConfigurationDto dto = mapper.map(configuration, REPOSITORY);

    assertThat(dto.isEnabled()).isTrue();
    assertThat(dto.getValidations()).isEmpty();
    assertThat(dto.getLinks().getLinkBy("self").get().getHref()).isEqualTo("scm/v2/commit-message-checker/configuration/" + REPOSITORY.getNamespaceAndName());
    assertThat(dto.getLinks().getLinkBy("availableValidators")).isNotPresent();
    assertThat(dto.getLinks().getLinkBy("update")).isNotPresent();
  }

  @Test
  void shouldMapRepoConfigDto() {
    ConfigurationDto dto = new ConfigurationDto();
    Configuration configuration = mapper.map(dto);

    assertThat(configuration.getValidations()).isNullOrEmpty();
    assertThat(configuration.isEnabled()).isFalse();
  }

  @Test
  void shouldMapValidation() {
    String validatorName = "mock";
    when(availableValidators.validatorFor(validatorName)).thenReturn(new SimpleValidator());

    Validation validation = new Validation(validatorName);
    ValidationDto dto = mapper.map(validation);

    assertThat(dto.getName()).isEqualTo(validatorName);
  }

  @Test
  void shouldMapComplexValidation() {
    String validatorName = "complex";
    when(availableValidators.validatorFor(validatorName)).thenReturn(new ConfiguredValidator());

    Validation validation = new Validation(validatorName);
    validation.setConfiguration(new ConfiguredValidatorConfig("abc", "master,develop"));
    ValidationDto dto = mapper.map(validation);

    assertThat(dto.getName()).isEqualTo(validatorName);
    assertThat(dto.getConfiguration()).hasToString("{\"pattern\":\"abc\",\"branches\":\"master,develop\"}");
  }

  @Test
  void shouldMapValidationDto() {
    String validatorName = "mock";
    when(availableValidators.validatorFor(validatorName)).thenReturn(new SimpleValidator());

    ValidationDto dto = new ValidationDto();
    dto.setName(validatorName);
    Validation validation = mapper.map(dto);

    assertThat(validation.getName()).isEqualTo(dto.getName());
  }

  @Test
  void shouldMapComplexValidationDto() throws JsonProcessingException {
    String validatorName = "mock";
    when(availableValidators.validatorFor(validatorName)).thenReturn(new ConfiguredValidator());

    ValidationDto dto = new ValidationDto();
    dto.setName(validatorName);
    dto.setConfiguration(new ObjectMapper().readTree("{\"pattern\":\"abc\",\"branches\":\"master,develop\"}"));
    Validation validation = mapper.map(dto);

    verify(configurationValidator).validate(any());
    assertThat(validation.getName()).isEqualTo(dto.getName());
    assertThat(validation.getConfiguration()).hasFieldOrProperty("pattern");
    assertThat(validation.getConfiguration()).hasFieldOrProperty("branches");
  }

  @Test
  void shouldFailOnMapComplexValidationDto() throws JsonProcessingException {
    String validatorName = "mock";
    when(availableValidators.validatorFor(validatorName)).thenReturn(new ConfiguredValidator());
    doThrow(new ConstraintViolationException("violations", null)).when(configurationValidator).validate(any());

    ValidationDto dto = new ValidationDto();
    dto.setName(validatorName);
    dto.setConfiguration(new ObjectMapper().readTree("{\"pattern\":\"abc\",\"branches\":\"master,develop\"}"));
    assertThrows(ConstraintViolationException.class, () -> mapper.map(dto));
  }

  static class SimpleValidator implements Validator {
    @Override
    public void validate(Context context, String commitMessage) {
    }
  }

  static class ConfiguredValidator implements Validator {

    @Override
    public Optional<Class<?>> getConfigurationType() {
      return Optional.of(ConfiguredValidatorConfig.class);
    }

    @Override
    public void validate(Context context, String commitMessage) {
      // Do nothing
    }
  }

  @Getter
  @Setter
  @NoArgsConstructor
  @AllArgsConstructor
  static class ConfiguredValidatorConfig {
    private String pattern;
    private String branches;
  }
}
