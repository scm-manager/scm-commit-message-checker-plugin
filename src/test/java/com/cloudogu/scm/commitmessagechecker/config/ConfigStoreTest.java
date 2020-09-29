package com.cloudogu.scm.commitmessagechecker.config;

import com.cloudogu.scm.commitmessagechecker.AvailableValidators;
import com.cloudogu.scm.commitmessagechecker.Context;
import com.cloudogu.scm.commitmessagechecker.Validator;
import com.google.common.collect.ImmutableList;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.plugin.PluginLoader;
import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryTestData;
import sonia.scm.store.InMemoryConfigurationStoreFactory;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ConfigStoreTest {

  private static final Repository REPOSITORY = RepositoryTestData.createHeartOfGold();

  @Mock
  private AvailableValidators availableValidators;
  @Mock
  private PluginLoader pluginLoader;

  private ConfigStore configStore;

  @BeforeEach
  void setUp() {
    configStore = new ConfigStore(availableValidators, InMemoryConfigurationStoreFactory.create(), pluginLoader);
  }

  @Test
  void shouldGetNewGlobalConfigFromStore() {
    GlobalConfiguration globalConfiguration = configStore.getGlobalConfiguration();

    assertThat(globalConfiguration.isEnabled()).isFalse();
    assertThat(globalConfiguration.isDisableRepositoryConfiguration()).isFalse();
    assertThat(globalConfiguration.getValidations()).isEmpty();
  }

  @Test
  void shouldGetStoredGlobalConfig() {
    GlobalConfiguration globalConfiguration = new GlobalConfiguration(true, ImmutableList.of(new Validation("TestValidator")), true);
    configStore.setGlobalConfiguration(globalConfiguration);

    GlobalConfiguration storedGlobalConfiguration = configStore.getGlobalConfiguration();

    assertThat(storedGlobalConfiguration.isEnabled()).isTrue();
    assertThat(storedGlobalConfiguration.isDisableRepositoryConfiguration()).isTrue();
    assertThat(storedGlobalConfiguration.getValidations()).hasSize(1);
  }

  @Test
  void shouldGetNewRepoConfigFromStore() {
    Configuration configuration = configStore.getConfiguration(REPOSITORY);

    assertThat(configuration.isEnabled()).isFalse();
    assertThat(configuration.getValidations()).isEmpty();
  }

  @Test
  void shouldGetStoredRepoConfig() {
    Configuration configuration = new Configuration(true, ImmutableList.of(new Validation("TestValidator")));
    configStore.setConfiguration(REPOSITORY, configuration);

    Configuration storedConfiguration = configStore.getConfiguration(REPOSITORY);

    assertThat(storedConfiguration.isEnabled()).isTrue();
    assertThat(storedConfiguration.getValidations()).hasSize(1);
  }

  @Test
  void shouldGetAllGlobalValidations() {
    when(availableValidators.validatorOf("TestValidator")).thenReturn(new TestValidator());
    GlobalConfiguration globalConfiguration = new GlobalConfiguration(true, ImmutableList.of(new Validation("TestValidator", new TestValidatorConfig("abc"))), true);
    configStore.setGlobalConfiguration(globalConfiguration);
    List<ConfigStore.ValidatorInstance> globalValidations = configStore.getGlobalValidations();

    assertThat(globalValidations).hasSize(1);
    assertThat(globalValidations.get(0).getValidator()).isInstanceOf(TestValidator.class);
    assertThat(globalValidations.get(0).getConfiguration()).isInstanceOf(TestValidatorConfig.class);
  }

  @Test
  void shouldGetAllValidationsForRepository() {
    when(availableValidators.validatorOf("TestValidator")).thenReturn(new TestValidator());
    Configuration configuration = new Configuration(true, ImmutableList.of(new Validation("TestValidator", new TestValidatorConfig("abc"))));
    configStore.setConfiguration(REPOSITORY, configuration);
    List<ConfigStore.ValidatorInstance> repoValidations = configStore.getValidations(REPOSITORY);

    assertThat(repoValidations).hasSize(1);
    assertThat(repoValidations.get(0).getValidator()).isInstanceOf(TestValidator.class);
    assertThat(repoValidations.get(0).getConfiguration()).isInstanceOf(TestValidatorConfig.class);
  }

  static class TestValidator implements Validator {

    @Override
    public boolean isApplicableMultipleTimes() {
      return false;
    }

    @Override
    public Optional<Class<?>> getConfigurationType() {
      return Optional.empty();
    }

    @Override
    public void validate(Context context, String commitMessage) {

    }
  }

  @AllArgsConstructor
  @NoArgsConstructor
  @Getter
  @Setter
  static class TestValidatorConfig {
    private String pattern;
  }
}
