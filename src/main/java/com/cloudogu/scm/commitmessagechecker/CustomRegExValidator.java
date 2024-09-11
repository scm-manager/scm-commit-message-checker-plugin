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

package com.cloudogu.scm.commitmessagechecker;

import com.google.common.base.Strings;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import sonia.scm.ContextEntry;
import sonia.scm.plugin.Extension;
import sonia.scm.util.GlobUtil;

import jakarta.validation.constraints.NotEmpty;
import jakarta.xml.bind.annotation.XmlRootElement;
import java.util.Arrays;
import java.util.Optional;
import java.util.regex.Pattern;

import static com.google.common.cache.CacheBuilder.newBuilder;

@Extension
public class CustomRegExValidator implements Validator {

  private static final String DEFAULT_ERROR_MESSAGE = "The commit message doesn't match the validation pattern.";

  private static final LoadingCache<String, Pattern> REGEX_CACHE =
    newBuilder().maximumSize(10).build(new CacheLoader<String, Pattern>() {
      @Override
      public Pattern load(String key) {
        return Pattern.compile(key);
      }
    });

  @Override
  public boolean isApplicableMultipleTimes() {
    return true;
  }

  @Override
  public Optional<Class<?>> getConfigurationType() {
    return Optional.of(CustomRegExValidatorConfig.class);
  }

  @Override
  public void validate(Context context, String commitMessage) {
    CustomRegExValidatorConfig configuration = context.getConfiguration(CustomRegExValidatorConfig.class);
    String commitBranch = context.getBranch();
    if (shouldValidateBranch(configuration, commitBranch) && isInvalidCommitMessage(configuration, commitMessage)) {
      throw new InvalidCommitMessageException(
        ContextEntry.ContextBuilder.entity(context.getRepository()),
        getErrorMessage(configuration)
      );
    }
  }

  private String getErrorMessage(CustomRegExValidatorConfig configuration) {
    if (Strings.isNullOrEmpty(configuration.getErrorMessage())) {
      return DEFAULT_ERROR_MESSAGE;
    }
    return configuration.getErrorMessage();
  }

  private boolean shouldValidateBranch(CustomRegExValidatorConfig configuration, String commitBranch) {
    if (Strings.isNullOrEmpty(commitBranch) || Strings.isNullOrEmpty(configuration.getBranches())) {
      return true;
    }
    return Arrays
      .stream(configuration.getBranches().split(","))
      .anyMatch(branch -> GlobUtil.matches(branch.trim(), commitBranch));
  }

  private boolean isInvalidCommitMessage(CustomRegExValidatorConfig configuration, String commitMessage) {
    return !REGEX_CACHE
      .getUnchecked(configuration.getPattern())
      .matcher(commitMessage)
      .find();
  }

  @AllArgsConstructor
  @NoArgsConstructor
  @Getter
  @Setter
  @XmlRootElement
  static class CustomRegExValidatorConfig {
    @NotEmpty
    @RegEx
    private String pattern;
    private String branches;
    private String errorMessage;
  }
}

