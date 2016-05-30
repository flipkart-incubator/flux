/*
 * Copyright 2012-2016, the original author or authors.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.flipkart.flux.loader;

import com.google.common.collect.Lists;
import org.reflections.vfs.Vfs;

import java.net.URL;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Helper class to suppress the warnings of reflections about url types.
 * Ex log:
 * [WARN] Reflections - could not create Vfs.Dir from url. ignoring the exception and continuing
 * org.reflections.ReflectionsException: could not create Vfs.Dir from url, no matching UrlType was found [file:/System/Library/Java/Extensions/libAppleScriptEngine.jnilib]
 * either use fromURL(final URL url, final List<UrlType> urlTypes) or use the static setDefaultURLTypes(final List<UrlType> urlTypes) or addDefaultURLTypes(UrlType urlType) with your specialized UrlType.
 *
 * @author shyam.akirala
 */
public class ReflectionsHelper {

    /**
     * Registers URLs with reflections. Currently our dev machines have .jnilib files in it's class path.
     * Reflections use of Vfs doesn't recognize the above URLs and logs warns when it sees them. By registering those file endings, we suppress the warns.
     */
    public static void registerUrlTypes() {

        final List<Vfs.UrlType> urlTypes = Lists.newArrayList();

        // include a list of file extensions / filenames to be recognized
        urlTypes.add(new EmptyIfFileEndingsUrlType(".jnilib"));

        urlTypes.addAll(Arrays.asList(Vfs.DefaultUrlTypes.values()));

        Vfs.setDefaultURLTypes(urlTypes);
    }

    private static class EmptyIfFileEndingsUrlType implements Vfs.UrlType {

        private final List<String> fileEndings;

        private EmptyIfFileEndingsUrlType(final String... fileEndings) {

            this.fileEndings = Lists.newArrayList(fileEndings);
        }

        public boolean matches(URL url) {

            final String protocol = url.getProtocol();
            final String externalForm = url.toExternalForm();
            if (!protocol.equals("file")) {
                return false;
            }
            for (String fileEnding : fileEndings) {
                if (externalForm.endsWith(fileEnding))
                    return true;
            }
            return false;
        }

        public Vfs.Dir createDir(final URL url) throws Exception {

            return emptyVfsDir(url);
        }

        private static Vfs.Dir emptyVfsDir(final URL url) {

            return new Vfs.Dir() {
                @Override
                public String getPath() {

                    return url.toExternalForm();
                }

                @Override
                public Iterable<Vfs.File> getFiles() {

                    return Collections.emptyList();
                }

                @Override
                public void close() {

                }
            };
        }
    }
}
