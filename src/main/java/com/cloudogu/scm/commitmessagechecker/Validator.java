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

import com.cloudogu.scm.commitmessagechecker.config.Validation;
import sonia.scm.plugin.ExtensionPoint;

import java.util.Optional;

/**
 * Each {@link Validator} class implementation defines a type of commit message validator.<br>
 * <br>
 * Validators applied to your repositories are represented by {@link Validation}s<br>
 * to support multiple {@link Validator}s of the same type with distinct configuration.
 */
@ExtensionPoint
public interface Validator {

  /**
   * Whether this {@link Validator} allows multiple {@link Validation}s to be created or only one.
   */
  default boolean isApplicableMultipleTimes() {
    return false;
  }

  /**
   * If a {@link Validator} is configurable, {@link #getConfigurationType()} describes the type of configuration.
   * The returned class has to be a serializable {@link jakarta.xml.bind.annotation.XmlRootElement}.
   */
  default Optional<Class<?>> getConfigurationType() {
    return Optional.empty();
  }

  /**
   * Implement this method to describe the concrete logic of your {@link Validator}.
   *
   * If the {@link Validator} is configurable, the {@link Validation}'s configuration is passed as part of the context.
   *
   * @throws InvalidCommitMessageException whenever the commit message does not pass this validator.
   */
  void validate(Context context, String commitMessage);
}
