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

package com.cloudogu.scm.commitmessagechecker.updates;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.store.ConfigurationStore;
import sonia.scm.store.InMemoryByteConfigurationStore;
import sonia.scm.store.InMemoryByteConfigurationStoreFactory;

import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class PartialRegexUpdateStepTest {

  private static final String STORE = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n" +
    "<commitMessageCheckerGlobalConfig>\n" +
    "    <enabled>true</enabled>\n" +
    "    <disableRepositoryConfiguration>true</disableRepositoryConfiguration>\n" +
    "    <validations>\n" +
    "        <name>JiraCommitMessageIssueKeyValidator</name>\n" +
    "        <configuration>\n" +
    "            <configurationType>sonia.scm.jira.commitmessagechecker.JiraCommitMessageIssueKeyValidator$JiraCommitMessageIssueKeyValidatorConfig</configurationType>\n" +
    "            <jiraCommitMessageIssueKeyValidatorConfig>\n" +
    "                <branches>hy</branches>\n" +
    "            </jiraCommitMessageIssueKeyValidatorConfig>\n" +
    "        </configuration>\n" +
    "    </validations>\n" +
    "    <validations>\n" +
    "        <name>CustomRegExValidator</name>\n" +
    "        <configuration>\n" +
    "            <configurationType>com.cloudogu.scm.commitmessagechecker.CustomRegExValidator$CustomRegExValidatorConfig</configurationType>\n" +
    "            <customRegExValidatorConfig>\n" +
    "                <branches>all</branches>\n" +
    "                <errorMessage>Tja</errorMessage>\n" +
    "                <pattern>abc</pattern>\n" +
    "            </customRegExValidatorConfig>\n" +
    "        </configuration>\n" +
    "    </validations>\n" +
    "</commitMessageCheckerGlobalConfig>\n";
  private static final String FIXED_STORE = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n" +
    "<commitMessageCheckerGlobalConfig>\n" +
    "    <enabled>true</enabled>\n" +
    "    <disableRepositoryConfiguration>true</disableRepositoryConfiguration>\n" +
    "    <validations>\n" +
    "        <name>JiraCommitMessageIssueKeyValidator</name>\n" +
    "        <configuration>\n" +
    "            <configurationType>sonia.scm.jira.commitmessagechecker.JiraCommitMessageIssueKeyValidator$JiraCommitMessageIssueKeyValidatorConfig</configurationType>\n" +
    "            <jiraCommitMessageIssueKeyValidatorConfig>\n" +
    "                <branches>hy</branches>\n" +
    "            </jiraCommitMessageIssueKeyValidatorConfig>\n" +
    "        </configuration>\n" +
    "    </validations>\n" +
    "    <validations>\n" +
    "        <name>CustomRegExValidator</name>\n" +
    "        <configuration>\n" +
    "            <configurationType>com.cloudogu.scm.commitmessagechecker.CustomRegExValidator$CustomRegExValidatorConfig</configurationType>\n" +
    "            <customRegExValidatorConfig>\n" +
    "                <branches>all</branches>\n" +
    "                <errorMessage>Tja</errorMessage>\n" +
    "                <pattern>^abc$</pattern>\n" +
    "            </customRegExValidatorConfig>\n" +
    "        </configuration>\n" +
    "    </validations>\n" +
    "</commitMessageCheckerGlobalConfig>\n";

  private final InMemoryByteConfigurationStoreFactory storeFactory = new InMemoryByteConfigurationStoreFactory();
  private final PartialRegexUpdateStep updateStep = new PartialRegexUpdateStep(storeFactory);

  @Test
  void shouldFixOldPatterns() throws NoSuchFieldException, IllegalAccessException {
    ConfigurationStore<PartialRegexUpdater.GlobalRootConfiguration> store = storeFactory
      .withType(PartialRegexUpdater.GlobalRootConfiguration.class)
      .withName("commitMessageChecker")
      .build();
    Field storeField = InMemoryByteConfigurationStore.class.getDeclaredField("store");
    storeField.setAccessible(true);
    storeField.set(store, STORE.getBytes(StandardCharsets.UTF_8));

    updateStep.doUpdate();

    String newContent = new String((byte[]) storeField.get(store));
    assertThat(newContent).isEqualTo(FIXED_STORE);
  }
}
