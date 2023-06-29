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

import javax.validation.constraints.NotEmpty;
import javax.xml.bind.annotation.XmlRootElement;
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

