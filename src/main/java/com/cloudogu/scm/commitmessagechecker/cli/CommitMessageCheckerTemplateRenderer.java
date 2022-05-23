package com.cloudogu.scm.commitmessagechecker.cli;

import sonia.scm.cli.CliContext;
import sonia.scm.cli.TemplateRenderer;
import sonia.scm.template.TemplateEngineFactory;

import javax.inject.Inject;
import java.util.Collections;

public class CommitMessageCheckerTemplateRenderer extends TemplateRenderer {
  private static final String INVALID_COMMIT_TEMPLATE = "{{i18n.invalidCommit}}: \n{{error}}";

  @Inject
  public CommitMessageCheckerTemplateRenderer(CliContext context, TemplateEngineFactory templateEngineFactory) {
    super(context, templateEngineFactory);
  }

  @Override
  public void renderDefaultError(Exception exception) {
    renderToStderr(INVALID_COMMIT_TEMPLATE, Collections.singletonMap("error",exception.getMessage()));
  }
}
