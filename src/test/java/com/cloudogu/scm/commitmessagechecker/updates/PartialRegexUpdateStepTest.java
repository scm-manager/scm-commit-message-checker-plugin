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
