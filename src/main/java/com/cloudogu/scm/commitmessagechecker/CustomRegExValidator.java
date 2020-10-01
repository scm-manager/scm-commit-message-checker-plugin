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

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.shiro.util.RegExPatternMatcher;
import sonia.scm.ContextEntry;
import sonia.scm.plugin.Extension;
import sonia.scm.util.GlobUtil;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.Arrays;
import java.util.Optional;

@Extension
public class CustomRegExValidator implements Validator {

  private static final RegExPatternMatcher matcher = new RegExPatternMatcher();

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
        "The Commit Message doesn't match the required format."
      );
    }
  }

  private boolean shouldValidateBranch(CustomRegExValidatorConfig configuration, String commitBranch) {
    return Arrays
        .stream(configuration.getBranches().split(","))
        .anyMatch(branch -> GlobUtil.matches(branch.trim(), commitBranch));
  }

  private boolean isInvalidCommitMessage(CustomRegExValidatorConfig configuration, String commitMessage) {
    return !matcher.matches(configuration.getPattern(), commitMessage);
  }

  @AllArgsConstructor
  @NoArgsConstructor
  @Getter
  @Setter
  @XmlRootElement
  static class CustomRegExValidatorConfig {
    @NotNull
    @NotBlank
    private String pattern;
    @NotNull
    @NotBlank
    private String branches;
    private String errorMessage;
  }
}

