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
import com.cloudogu.scm.commitmessagechecker.Validator;
import com.google.common.annotations.VisibleForTesting;
import com.google.inject.Inject;
import de.otto.edison.hal.HalRepresentation;
import de.otto.edison.hal.Links;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Getter;
import sonia.scm.api.v2.resources.ErrorDto;
import sonia.scm.api.v2.resources.LinkBuilder;
import sonia.scm.web.VndMediaType;

import javax.validation.Valid;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@OpenAPIDefinition(tags = {
  @Tag(name = "Commit Message Checker Plugin", description = "Commit Message Checker plugin provided endpoints")
})
@Path("v2/commit-message-checker/configuration")
public class ConfigurationResource {

  @VisibleForTesting
  public static final String MEDIA_TYPE = VndMediaType.PREFIX + "commitMessageChecker" + VndMediaType.SUFFIX;

  private final ConfigurationService configurationService;
  private final Set<Validator> availableValidators;

  @Inject
  public ConfigurationResource(ConfigurationService configurationService, Set<Validator> availableValidators) {
    this.configurationService = configurationService;
    this.availableValidators = availableValidators;
  }

  @PUT
  @Path("/{namespace}/{name}")
  @Consumes(MEDIA_TYPE)
  @Operation(
    summary = "Update repository-specific commit message checker configuration",
    description = "Modifies the repository-specific commit message checker configuration.",
    tags = "Commit Message Checker Plugin",
    operationId = "commit_message_checker_update_repo_config"
  )
  @ApiResponse(responseCode = "204", description = "update success")
  @ApiResponse(responseCode = "401", description = "not authenticated / invalid credentials")
  @ApiResponse(responseCode = "403", description = "not authorized /  the current user does not have the right privilege")
  @ApiResponse(
    responseCode = "500",
    description = "internal server error",
    content = @Content(
      mediaType = VndMediaType.ERROR_TYPE,
      schema = @Schema(implementation = ErrorDto.class)
    )
  )
  public Response updateConfiguration(@PathParam("namespace") String namespace, @PathParam("name") String name, @Valid ConfigurationDto updatedConfig) {
    configurationService.updateRepositoryConfiguration(namespace, name, updatedConfig);
    return Response.noContent().build();
  }

  @GET
  @Path("/{namespace}/{name}")
  @Produces(MEDIA_TYPE)
  @Operation(
    summary = "Get repository-specific commit message checker configuration",
    description = "Returns the repository-specific commit message checker configuration.",
    tags = "Commit Message Checker Plugin",
    operationId = "commit_message_checker_get_repo_config")
  @ApiResponse(
    responseCode = "200",
    description = "success",
    content = @Content(
      mediaType = MediaType.APPLICATION_JSON,
      schema = @Schema(implementation = ConfigurationDto.class)
    )
  )
  @ApiResponse(responseCode = "401", description = "not authenticated / invalid credentials")
  @ApiResponse(responseCode = "403", description = "not authorized /  the current user does not have the right privilege")
  @ApiResponse(
    responseCode = "404",
    description = "not found / no repository available for given parameters",
    content = @Content(
      mediaType = VndMediaType.ERROR_TYPE,
      schema = @Schema(implementation = ErrorDto.class)
    )
  )
  @ApiResponse(
    responseCode = "500",
    description = "internal server error",
    content = @Content(
      mediaType = VndMediaType.ERROR_TYPE,
      schema = @Schema(implementation = ErrorDto.class)
    )
  )
  public Response getConfiguration(@PathParam("namespace") String namespace, @PathParam("name") String name) {
    return Response.ok(configurationService.getRepositoryConfiguration(namespace, name)).build();
  }

  @PUT
  @Path("/")
  @Consumes(MEDIA_TYPE)
  @Operation(
    summary = "Update global commit message checker configuration",
    description = "Modifies the global commit message checker configuration.",
    tags = "Commit Message Checker Plugin",
    operationId = "commit_message_checker_update_global_config"
  )
  @ApiResponse(responseCode = "204", description = "update success")
  @ApiResponse(responseCode = "401", description = "not authenticated / invalid credentials")
  @ApiResponse(responseCode = "403", description = "not authorized /  the current user does not have the right privilege")
  @ApiResponse(
    responseCode = "500",
    description = "internal server error",
    content = @Content(
      mediaType = VndMediaType.ERROR_TYPE,
      schema = @Schema(implementation = ErrorDto.class)
    )
  )
  public Response updateGlobalConfiguration(@Valid GlobalConfigurationDto updatedConfig) {
    configurationService.updateGlobalConfiguration(updatedConfig);
    return Response.noContent().build();
  }

  @GET
  @Path("/")
  @Produces(MEDIA_TYPE)
  @Operation(
    summary = "Get global commit message checker configuration",
    description = "Returns the global commit message checker configuration.",
    tags = "Commit Message Checker Plugin",
    operationId = "commit_message_checker_get_global_config"
  )
  @ApiResponse(
    responseCode = "200",
    description = "success",
    content = @Content(
      mediaType = MediaType.APPLICATION_JSON,
      schema = @Schema(implementation = ConfigurationDto.class)
    )
  )
  @ApiResponse(responseCode = "401", description = "not authenticated / invalid credentials")
  @ApiResponse(responseCode = "403", description = "not authorized /  the current user does not have the right privilege")
  @ApiResponse(
    responseCode = "500",
    description = "internal server error",
    content = @Content(
      mediaType = VndMediaType.ERROR_TYPE,
      schema = @Schema(implementation = ErrorDto.class)
    )
  )
  public Response getGlobalConfiguration() {
    return Response.ok(configurationService.getGlobalConfiguration()).build();
  }

  @GET
  @Path("validators")
  @Produces({MediaType.APPLICATION_JSON})
  @Operation(
    summary = "Commit Message Validators",
    description = "Returns available validator for commit message validation.",
    tags = "Commit Message Checker",
    operationId = "commit_message_checker_get_all_available_validators"
  )
  @ApiResponse(
    responseCode = "200",
    description = "success",
    content = @Content(
      mediaType = MediaType.APPLICATION_JSON,
      schema = @Schema(implementation = HalRepresentation.class)
    )
  )
  @ApiResponse(
    responseCode = "500",
    description = "internal server error",
    content = @Content(
      mediaType = VndMediaType.ERROR_TYPE,
      schema = @Schema(implementation = ErrorDto.class)
    )
  )
  public AvailableValidatorsDto getAvailableValidators(@Context UriInfo uriInfo) {
    LinkBuilder linkBuilder = new LinkBuilder(uriInfo::getBaseUri, ConfigurationResource.class);
    final String selfLink = linkBuilder.method("getAvailableValidators").parameters().href();
    return new AvailableValidatorsDto(
      new Links.Builder().self(selfLink).build(),
      availableValidators.stream().map(ValidatorDto::new).collect(Collectors.toList())
    );
  }

  @Getter
  @SuppressWarnings("java:S2160") // wo do not need equals and hashcode for dto
  static class AvailableValidatorsDto extends HalRepresentation {
    private final List<ValidatorDto> validators;

    public AvailableValidatorsDto(Links links, List<ValidatorDto> validators) {
      super(links);
      this.validators = validators;
    }
  }

  @Getter
  static class ValidatorDto {
    private final String name;
    private final boolean applicableMultipleTimes;

    ValidatorDto(Validator validator) {
      this.name = AvailableValidators.nameOf(validator);
      this.applicableMultipleTimes = validator.isApplicableMultipleTimes();
    }
  }
}
