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

package com.cloudogu.scm.commitmessagechecker.cli;

import sonia.scm.cli.CliContext;
import sonia.scm.cli.ExitCode;
import sonia.scm.cli.TemplateRenderer;
import sonia.scm.template.TemplateEngineFactory;

import jakarta.inject.Inject;
import java.util.Collections;

import static java.util.Collections.emptyMap;

public class CommitMessageCheckerTemplateRenderer extends TemplateRenderer {
  private static final String INVALID_COMMIT_TEMPLATE = "{{i18n.invalidCommit}}:\n{{error}}";
  private static final String NOT_FOUND_TEMPLATE = "{{i18n.repoNotFound}}";

  private final CliContext context;

  @Inject
  public CommitMessageCheckerTemplateRenderer(CliContext context, TemplateEngineFactory templateEngineFactory) {
    super(context, templateEngineFactory);
    this.context = context;
  }

  @Override
  public void renderDefaultError(Exception exception) {
    renderToStderr(INVALID_COMMIT_TEMPLATE, Collections.singletonMap("error",exception.getMessage()));
  }

  public void renderNotFoundError() {
    renderToStderr(NOT_FOUND_TEMPLATE, emptyMap());
    context.exit(ExitCode.NOT_FOUND);
  }
}
