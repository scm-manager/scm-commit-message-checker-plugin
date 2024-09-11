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

import lombok.Getter;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAnyElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import java.util.Collection;
import java.util.List;

final class PartialRegexUpdater {

  private PartialRegexUpdater() {
  }

  static <T extends RootConfiguration> T doUpdate(T config) {
    if (config.getValidations() != null) {
      config.getValidations().forEach(PartialRegexUpdater::updateSubConfig);
    }
    return config;
  }

  private static void updateSubConfig(Validation subConfig) {
    if ("com.cloudogu.scm.commitmessagechecker.CustomRegExValidator$CustomRegExValidatorConfig".equals(subConfig.getConfiguration().getConfigurationType())) {
      NodeList childNodes = subConfig.getConfiguration().getConfiguration().get(0).getChildNodes();
      for (int i = 0; i < childNodes.getLength(); ++i) {
        Node item = childNodes.item(i);
        if (item.getNodeName().equals("pattern")) {
          updatePatternNode(item);
        }
      }
    }
  }

  private static void updatePatternNode(Node item) {
    String oldRegex = item.getTextContent();
    if (!oldRegex.startsWith("^") && !oldRegex.endsWith("$")) {
      item.setTextContent("^" + oldRegex + "$");
    }
  }

  @Getter
  @XmlAccessorType(XmlAccessType.FIELD)
  static class RootConfiguration {
    @XmlAnyElement
    private List<Element> rest;
    private Collection<Validation> validations;
  }

  @XmlRootElement(name = "commitMessageCheckerConfig")
  static class RepositoryRootConfiguration extends RootConfiguration {
  }

  @XmlRootElement(name = "commitMessageCheckerGlobalConfig")
  static class GlobalRootConfiguration extends RootConfiguration {
  }

  @XmlRootElement
  @XmlAccessorType(XmlAccessType.FIELD)
  @Getter
  static class Validation {
    private String name;
    private Configuration configuration;
  }

  @XmlRootElement
  @XmlAccessorType(XmlAccessType.FIELD)
  @Getter
  static class Configuration {
    private String configurationType;
    @XmlAnyElement
    private List<Element> configuration;
  }
}
