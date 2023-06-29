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
