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

import javax.ws.rs.core.UriInfo;
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
    assertThat(response.getContentAsString()).isEqualTo("{\"enabled\":false,\"validations\":[],\"disableRepositoryConfiguration\":false}");
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
    assertThat(response.getContentAsString()).isEqualTo("{\"enabled\":false,\"validations\":[]}");
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
