package com.cloudogu.scm.commitmessagechecker.config;

import org.apache.shiro.authz.AuthorizationException;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.util.ThreadContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryManager;
import sonia.scm.repository.RepositoryTestData;

import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ConfigurationServiceTest {

  private static final Repository REPOSITORY = RepositoryTestData.createHeartOfGold();

  @Mock
  private ConfigStore configStore;
  @Mock
  private RepositoryManager repositoryManager;
  @Mock
  private ConfigurationMapper mapper;
  @Mock
  private Subject subject;

  @InjectMocks
  private ConfigurationService service;

  @BeforeEach
  void setUp() {
    ThreadContext.bind(subject);
  }

  @AfterEach
  void tearDown() {
    ThreadContext.unbindSubject();
  }

  @Test
  void shouldGetRepoConfig() {
    when(repositoryManager.get(REPOSITORY.getNamespaceAndName())).thenReturn(REPOSITORY);
    Configuration configuration = new Configuration();
    when(configStore.getConfiguration(REPOSITORY)).thenReturn(configuration);
    when(mapper.map(configuration, REPOSITORY)).thenReturn(new ConfigurationDto());

    ConfigurationDto repositoryConfiguration = service.getRepositoryConfiguration(REPOSITORY.getNamespace(), REPOSITORY.getName());

    assertThat(repositoryConfiguration.isEnabled()).isFalse();
    assertThat(repositoryConfiguration.getValidations()).isEmpty();
  }

  @Test
  void shouldNotGetRepoConfigIfNotPermitted() {
    doThrow(AuthorizationException.class).when(subject).checkPermission("repository:commitMessageChecker:" + REPOSITORY.getId());
    when(repositoryManager.get(REPOSITORY.getNamespaceAndName())).thenReturn(REPOSITORY);

    assertThrows(AuthorizationException.class, () -> service.getRepositoryConfiguration(REPOSITORY.getNamespace(), REPOSITORY.getName()));
  }

  @Test
  void shouldUpdateRepoConfig() {
    when(repositoryManager.get(REPOSITORY.getNamespaceAndName())).thenReturn(REPOSITORY);
    ConfigurationDto dto = new ConfigurationDto(true, emptyList());
    Configuration configuration = new Configuration(true, emptyList());
    when(mapper.map(dto)).thenReturn(configuration);
    service.updateRepositoryConfiguration(REPOSITORY.getNamespace(), REPOSITORY.getName(), dto);

    verify(configStore).setConfiguration(REPOSITORY, configuration);
  }

  @Test
  void shouldNotUpdateRepoConfigIfNotPermitted() {
    when(repositoryManager.get(REPOSITORY.getNamespaceAndName())).thenReturn(REPOSITORY);
    doThrow(AuthorizationException.class).when(subject).checkPermission("repository:commitMessageChecker:" + REPOSITORY.getId());

    assertThrows(AuthorizationException.class, () -> service.updateRepositoryConfiguration(REPOSITORY.getNamespace(), REPOSITORY.getName(), new ConfigurationDto()));
  }

  @Test
  void shouldGetGlobalConfig() {
    GlobalConfiguration configuration = new GlobalConfiguration();
    when(configStore.getGlobalConfiguration()).thenReturn(configuration);
    when(mapper.map(configuration)).thenReturn(new GlobalConfigurationDto());

    GlobalConfigurationDto repositoryConfiguration = service.getGlobalConfiguration();

    assertThat(repositoryConfiguration.isEnabled()).isFalse();
    assertThat(repositoryConfiguration.getValidations()).isEmpty();
  }

  @Test
  void shouldNotGetGlobalConfigIfNotPermitted() {
    doThrow(AuthorizationException.class).when(subject).checkPermission("configuration:read:commitMessageChecker");
    assertThrows(AuthorizationException.class, () -> service.getGlobalConfiguration());
  }

  @Test
  void shouldUpdateGlobalConfig() {
    GlobalConfigurationDto dto = new GlobalConfigurationDto();
    GlobalConfiguration configuration = new GlobalConfiguration();
    when(mapper.map(dto)).thenReturn(configuration);
    service.updateGlobalConfiguration(dto);

    verify(configStore).setGlobalConfiguration(configuration);
  }

  @Test
  void shouldNotUpdateGlobalConfigIfNotPermitted() {
    doThrow(AuthorizationException.class).when(subject).checkPermission("configuration:write:commitMessageChecker");
    assertThrows(AuthorizationException.class, () -> service.updateGlobalConfiguration(new GlobalConfigurationDto()));
  }
}
