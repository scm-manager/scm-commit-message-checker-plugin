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

import com.cloudogu.scm.commitmessagechecker.updates.PartialRegexUpdater.RepositoryRootConfiguration;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.migration.RepositoryUpdateContext;
import sonia.scm.store.ConfigurationStore;
import sonia.scm.store.InMemoryByteConfigurationStore;
import sonia.scm.store.InMemoryByteConfigurationStoreFactory;

import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PartialRegexRepositoryUpdateStepTest {

  private static final String STORE = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n" +
    "<commitMessageCheckerConfig>\n" +
    "    <enabled>true</enabled>\n" +
    "    <validations>\n" +
    "        <name>CustomRegExValidator</name>\n" +
    "        <configuration>\n" +
    "            <configurationType>com.cloudogu.scm.commitmessagechecker.CustomRegExValidator$CustomRegExValidatorConfig</configurationType>\n" +
    "            <customRegExValidatorConfig>\n" +
    "                <branches/>\n" +
    "                <errorMessage>At least 5</errorMessage>\n" +
    "                <pattern>.{5,72}</pattern>\n" +
    "            </customRegExValidatorConfig>\n" +
    "        </configuration>\n" +
    "    </validations>\n" +
    "    <validations>\n" +
    "        <name>CustomRegExValidator</name>\n" +
    "        <configuration>\n" +
    "            <configurationType>com.cloudogu.scm.commitmessagechecker.CustomRegExValidator$CustomRegExValidatorConfig</configurationType>\n" +
    "            <customRegExValidatorConfig>\n" +
    "                <branches>master</branches>\n" +
    "                <errorMessage>Irgendwas</errorMessage>\n" +
    "                <pattern>.*</pattern>\n" +
    "            </customRegExValidatorConfig>\n" +
    "        </configuration>\n" +
    "    </validations>\n" +
    "</commitMessageCheckerConfig>\n";
  private static final String FIXED_STORE = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n" +
    "<commitMessageCheckerConfig>\n" +
    "    <enabled>true</enabled>\n" +
    "    <validations>\n" +
    "        <name>CustomRegExValidator</name>\n" +
    "        <configuration>\n" +
    "            <configurationType>com.cloudogu.scm.commitmessagechecker.CustomRegExValidator$CustomRegExValidatorConfig</configurationType>\n" +
    "            <customRegExValidatorConfig>\n" +
    "                <branches/>\n" +
    "                <errorMessage>At least 5</errorMessage>\n" +
    "                <pattern>^.{5,72}$</pattern>\n" +
    "            </customRegExValidatorConfig>\n" +
    "        </configuration>\n" +
    "    </validations>\n" +
    "    <validations>\n" +
    "        <name>CustomRegExValidator</name>\n" +
    "        <configuration>\n" +
    "            <configurationType>com.cloudogu.scm.commitmessagechecker.CustomRegExValidator$CustomRegExValidatorConfig</configurationType>\n" +
    "            <customRegExValidatorConfig>\n" +
    "                <branches>master</branches>\n" +
    "                <errorMessage>Irgendwas</errorMessage>\n" +
    "                <pattern>^.*$</pattern>\n" +
    "            </customRegExValidatorConfig>\n" +
    "        </configuration>\n" +
    "    </validations>\n" +
    "</commitMessageCheckerConfig>\n";

  private final InMemoryByteConfigurationStoreFactory storeFactory = new InMemoryByteConfigurationStoreFactory();
  private final PartialRegexRepositoryUpdateStep updateStep = new PartialRegexRepositoryUpdateStep(storeFactory);

  @Mock
  private RepositoryUpdateContext context;

  @Test
  void shouldFixOldPatterns() throws NoSuchFieldException, IllegalAccessException {
    ConfigurationStore<RepositoryRootConfiguration> store = storeFactory
      .withType(RepositoryRootConfiguration.class)
      .withName("commitMessageChecker")
      .forRepository("42")
      .build();
    Field storeField = InMemoryByteConfigurationStore.class.getDeclaredField("store");
    storeField.setAccessible(true);
    storeField.set(store, STORE.getBytes(StandardCharsets.UTF_8));

    when(context.getRepositoryId()).thenReturn("42");

    updateStep.doUpdate(context);

    String newContent = new String((byte[]) storeField.get(store));
    assertThat(newContent).isEqualTo(FIXED_STORE);
  }

  @Test
  void shouldNotFixPatternsWithStartAndEnd() throws NoSuchFieldException, IllegalAccessException {
    ConfigurationStore<RepositoryRootConfiguration> store = storeFactory
      .withType(RepositoryRootConfiguration.class)
      .withName("commitMessageChecker")
      .forRepository("42")
      .build();
    Field storeField = InMemoryByteConfigurationStore.class.getDeclaredField("store");
    storeField.setAccessible(true);
    storeField.set(store, FIXED_STORE.getBytes(StandardCharsets.UTF_8));

    when(context.getRepositoryId()).thenReturn("42");

    updateStep.doUpdate(context);

    String newContent = new String((byte[]) storeField.get(store));
    assertThat(newContent).isEqualTo(FIXED_STORE);
  }
}
