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

import com.cloudogu.scm.commitmessagechecker.Context;
import com.cloudogu.scm.commitmessagechecker.Validator;
import org.jboss.resteasy.mock.MockHttpRequest;
import org.jboss.resteasy.mock.MockHttpResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryTestData;
import sonia.scm.web.RestDispatcher;

import jakarta.ws.rs.core.UriInfo;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;

import static com.cloudogu.scm.commitmessagechecker.config.ConfigurationResource.MEDIA_TYPE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ConfigurationResourceTest {

  private static final Repository REPOSITORY = RepositoryTestData.createHeartOfGold();

  @Mock
  private ConfigurationService service;
  @Mock
  private UriInfo uriInfo;

  private RestDispatcher dispatcher;
  private final MockHttpResponse response = new MockHttpResponse();
  private Set<Validator> availableValidators;

  @BeforeEach
  void init() {
    availableValidators = new LinkedHashSet<>();
    ConfigurationResource resource = new ConfigurationResource(service, availableValidators);

    dispatcher = new RestDispatcher();
    dispatcher.addSingletonResource(resource);

    lenient().when(uriInfo.getBaseUri()).thenReturn(URI.create("localhost/scm/api"));
  }

  @Test
  void shouldGetGlobalConfig() throws URISyntaxException, UnsupportedEncodingException {
    MockHttpRequest request = MockHttpRequest.get("/v2/commit-message-checker/configuration");
    when(service.getGlobalConfiguration()).thenReturn(new GlobalConfigurationDto());

    dispatcher.invoke(request, response);

    assertThat(response.getStatus()).isEqualTo(200);
    assertThat(response.getContentAsString()).isEqualTo("{\"enabled\":false,\"validations\":null,\"disableRepositoryConfiguration\":false}");
  }

  @Test
  void shouldSetGlobalConfig() throws URISyntaxException {
    MockHttpRequest request = MockHttpRequest.put("/v2/commit-message-checker/configuration")
      .content("{\"enabled\":false,\"validations\":null,\"disableRepositoryConfiguration\":false}".getBytes())
      .contentType(MEDIA_TYPE);

    dispatcher.invoke(request, response);

    verify(service).updateGlobalConfiguration(new GlobalConfigurationDto());
    assertThat(response.getStatus()).isEqualTo(204);
  }

  @Test
  void shouldGetRepoConfig() throws URISyntaxException, UnsupportedEncodingException {
    MockHttpRequest request = MockHttpRequest.get("/v2/commit-message-checker/configuration/" + REPOSITORY.getNamespaceAndName());
    when(service.getRepositoryConfiguration(REPOSITORY.getNamespace(), REPOSITORY.getName())).thenReturn(new ConfigurationDto());

    dispatcher.invoke(request, response);

    assertThat(response.getStatus()).isEqualTo(200);
    assertThat(response.getContentAsString()).isEqualTo("{\"enabled\":false,\"validations\":null}");
  }

  @Test
  void shouldSetRepoConfig() throws URISyntaxException {
    MockHttpRequest request = MockHttpRequest.put("/v2/commit-message-checker/configuration/" + REPOSITORY.getNamespaceAndName())
      .content("{\"enabled\":false,\"validations\":null}".getBytes())
      .contentType(MEDIA_TYPE);

    dispatcher.invoke(request, response);

    verify(service).updateRepositoryConfiguration(REPOSITORY.getNamespace(), REPOSITORY.getName(), new ConfigurationDto());
    assertThat(response.getStatus()).isEqualTo(204);
  }

  @Test
  void shouldGetAllAvailableValidators() throws URISyntaxException, UnsupportedEncodingException {
    MockHttpRequest request = MockHttpRequest.get("/v2/commit-message-checker/configuration/validators");
    availableValidators.add(new MockValidator());
    dispatcher.invoke(request, response);

    assertThat(response.getStatus()).isEqualTo(200);
    assertThat(response.getContentAsString()).isEqualTo("{\"validators\":[{\"name\":\"MockValidator\",\"applicableMultipleTimes\":false}],\"_links\":{\"self\":{\"href\":\"/v2/commit-message-checker/configuration/validators\"}}}");
  }

  static class MockValidator implements Validator {
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
      // Do nothing
    }
  }
}
