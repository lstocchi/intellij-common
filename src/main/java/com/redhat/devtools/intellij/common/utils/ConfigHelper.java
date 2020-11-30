/*******************************************************************************
 * Copyright (c) 2019 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v20.html
 *
 * Contributors:
 * Red Hat, Inc. - initial API and implementation
 ******************************************************************************/
package com.redhat.devtools.intellij.common.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import io.fabric8.kubernetes.api.model.Config;
import io.fabric8.kubernetes.api.model.ConfigBuilder;
import io.fabric8.kubernetes.api.model.Context;
import io.fabric8.kubernetes.api.model.NamedContext;
import io.fabric8.kubernetes.client.internal.KubeConfigUtils;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

public class ConfigHelper {
    private static final ObjectMapper mapper = new ObjectMapper(new YAMLFactory());

    public static String getKubeConfigPath() {
        return System.getProperty("user.home") + "/.kube/config";
    }

    public static void saveKubeConfig(Config config) throws IOException {
        mapper.writeValue(new File(getKubeConfigPath()), config);
    }

    public static Config safeLoadKubeConfig() {
        try {
            return loadKubeConfig();
        } catch (IOException e) {
            return null;
        }
    }

    public static Config loadKubeConfig() throws IOException {
        return loadKubeConfig(getKubeConfigPath());
    }

    public static Config loadKubeConfig(String path) throws IOException {
        File f = new File(path);
        if (f.exists()) {
            return KubeConfigUtils.parseConfig(f);
        } else {
            return new ConfigBuilder().build();
        }
    }

    public static boolean isKubeConfigParsable() {
        return isKubeConfigParsable(new File(getKubeConfigPath()));
    }

    public static boolean isKubeConfigParsable(File kubeConfig) {
        try {
            mapper.readValue(kubeConfig, Config.class);
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    public static ToolsConfig loadToolsConfig() throws IOException {
        return loadToolsConfig(ConfigHelper.class.getResource("/tools.json"));
    }

    public static ToolsConfig loadToolsConfig(URL url) throws IOException {
        return mapper.readValue(url, ToolsConfig.class);
    }

    public static Context getCurrentContext() {
        try {
            Config config = loadKubeConfig();
            return KubeConfigUtils.getCurrentContext(config);
        } catch (IOException e) {
            return null;
        }
    }

    /**
     * Returns {@code true} if both given contexts are equal. They are considered equal if they're equal in
     * <ul>
     *     <li>cluster</li>
     *     <li>user</li>
     *     <li>current namespace</li>
     *     <li>extensions</li>
     * </ul>
     *
     *
     * @param thisContext
     * @param thatContext
     * @return true if both contexts are equal
     *
     * @see NamedContext
     * @see Context
     */
    public static boolean areEqual(NamedContext thisContext, NamedContext thatContext) {
        if (thisContext == null) {
            return thatContext == null;
        } else if (thatContext == null) {
            return false;
        }

        return Objects.equals(thisContext.getContext(), thatContext.getContext());
    }

    public static boolean areEqual(Collection<NamedContext> these, Collection<NamedContext> those) {
        return Objects.equals(these, those);
    }

}
