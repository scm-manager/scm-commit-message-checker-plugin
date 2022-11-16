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

import com.cloudogu.scm.commitmessagechecker.AvailableValidators;
import com.cloudogu.scm.commitmessagechecker.CommitMessageCheckerPermissions;
import com.cloudogu.scm.commitmessagechecker.Constants;
import com.cloudogu.scm.commitmessagechecker.Validator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.otto.edison.hal.Link;
import de.otto.edison.hal.Links;
import org.mapstruct.AfterMapping;
import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import sonia.scm.api.v2.resources.BaseMapper;
import sonia.scm.api.v2.resources.LinkBuilder;
import sonia.scm.api.v2.resources.ScmPathInfoStore;
import sonia.scm.config.ConfigurationPermissions;
import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryPermissions;

import javax.inject.Inject;

import static com.cloudogu.scm.commitmessagechecker.Constants.NAME;
import static com.cloudogu.scm.commitmessagechecker.Constants.READ_COMMIT_MESSAGE_CHECKER_PERMISSION;
import static com.cloudogu.scm.commitmessagechecker.Constants.WRITE_COMMIT_MESSAGE_CHECKER_PERMISSION;
import static de.otto.edison.hal.Link.link;
import static de.otto.edison.hal.Links.linkingTo;

@Mapper
@SuppressWarnings("java:S3740")
// we don't provide types to basemapper because we use it for global and local configuration
public abstract class ConfigurationMapper extends BaseMapper {

  @Inject
  AvailableValidators availableValidators;

  @Inject
  ConfigurationValidator configurationValidator;

  @Inject
  private ScmPathInfoStore scmPathInfoStore;

  public abstract ConfigurationDto map(Configuration configuration, @Context Repository repository);

  public abstract Configuration map(ConfigurationDto configurationDto);

  public abstract GlobalConfigurationDto map(GlobalConfiguration configuration);

  public abstract GlobalConfiguration map(GlobalConfigurationDto configurationDto);

  ValidationDto map(Validation validation) {
    ValidationDto dto = new ValidationDto();
    dto.setName(validation.getName());
    Validator validator = availableValidators.validatorFor(validation.getName());
    if (validator.getConfigurationType().isPresent()) {
      dto.setConfiguration(new ObjectMapper().valueToTree(validation.getConfiguration()));
    }
    return dto;
  }

  Validation map(ValidationDto dto) {
    Validation validation = new Validation();
    Validator validator = availableValidators.validatorFor(dto.getName());
    validation.setName(dto.getName());
    validator.getConfigurationType()
      .ifPresent(configurationType -> validation.setConfiguration(parseConfiguration(dto, validator, configurationType)));
    return validation;
  }

  @AfterMapping
  public void addLinks(@MappingTarget GlobalConfigurationDto target) {
    Links.Builder linksBuilder = linkingTo().self(globalSelf());
    if (ConfigurationPermissions.write(Constants.NAME).isPermitted()) {
      linksBuilder.single(Link.link("update", globalUpdate()));
    }
    if (ConfigurationPermissions.read(NAME).isPermitted()
      || ConfigurationPermissions.write(NAME).isPermitted()) {
      linksBuilder.single(link("availableValidators", availableValidators()));
    }
    target.add(linksBuilder.build());
  }

  private String availableValidators() {
    LinkBuilder linkBuilder = new LinkBuilder(scmPathInfoStore.get(), ConfigurationResource.class);
    return linkBuilder.method("getAvailableValidators").parameters().href();
  }

  private String globalSelf() {
    LinkBuilder linkBuilder = new LinkBuilder(scmPathInfoStore.get(), ConfigurationResource.class);
    return linkBuilder.method("getGlobalConfiguration").parameters().href();
  }

  private String globalUpdate() {
    LinkBuilder linkBuilder = new LinkBuilder(scmPathInfoStore.get(), ConfigurationResource.class);
    return linkBuilder.method("updateGlobalConfiguration").parameters().href();
  }

  @AfterMapping
  public void addLinks(@MappingTarget ConfigurationDto target, @Context Repository repository) {
    Links.Builder linksBuilder = linkingTo().self(self(repository));
    if (RepositoryPermissions.custom(WRITE_COMMIT_MESSAGE_CHECKER_PERMISSION, repository).isPermitted()) {
      linksBuilder.single(Link.link("update", update(repository)));
    }
    if (CommitMessageCheckerPermissions.mayRead(repository)) {
      linksBuilder.single(link("availableValidators", availableValidators()));
    }
    target.add(linksBuilder.build());
  }

  private String self(Repository repository) {
    LinkBuilder linkBuilder = new LinkBuilder(scmPathInfoStore.get(), ConfigurationResource.class);
    return linkBuilder.method("getConfiguration").parameters(repository.getNamespace(), repository.getName()).href();
  }

  private String update(Repository repository) {
    LinkBuilder linkBuilder = new LinkBuilder(scmPathInfoStore.get(), ConfigurationResource.class);
    return linkBuilder.method("updateConfiguration").parameters(repository.getNamespace(), repository.getName()).href();
  }

  private Object parseConfiguration(ValidationDto dto, Validator validator, Class<?> configurationType) {
    Object configuration;
    try {
      configuration = new ObjectMapper().treeToValue(dto.getConfiguration(), configurationType);
      configurationValidator.validate(configuration);
      return configuration;
    } catch (JsonProcessingException e) {
      throw new InvalidConfigurationException(validator, e);
    }
  }
}

