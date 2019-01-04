/*  
 * Copyright IBM Corp. 2018
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.ibm.g11n.pipeline.resfilter.impl;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;

import com.ibm.g11n.pipeline.resfilter.FilterInfo;
import com.ibm.g11n.pipeline.resfilter.FilterInfo.Type;
import com.ibm.g11n.pipeline.resfilter.MultiBundleResourceFilter;
import com.ibm.g11n.pipeline.resfilter.ResourceFilter;
import com.ibm.g11n.pipeline.resfilter.ResourceFilterProvider;
import com.ibm.g11n.pipeline.resfilter.impl.JavaPropertiesResource.Encoding;
import com.ibm.g11n.pipeline.resfilter.impl.JavaPropertiesResource.MessagePatternEscape;

/**
 * The default {@link ResourceFilterProvider} implementation.
 * 
 * @author yoshito_umaoka
 */
public class DefaultResourceFilterProvider extends ResourceFilterProvider {
    enum Filter {
        AMDJS,
        ANDROID,
        GLOBALIZEJS,
        IOS,
        JAVA,
        JAVAMSG,
        JAVAMSGUTF8,
        JAVAUTF8,
        JSON,
        PO,
        POT,
        XLIFF,
        YML;

        FilterInfo getFilterInfo() {
            return new FilterInfo(Type.SINGLE, name());
        }
    }

    private static final Map<String, FilterInfo> FILTERS = new HashMap<>(Filter.values().length);
    static {
        for (Filter filter : Filter.values()) {
            FILTERS.put(filter.name(), filter.getFilterInfo());
        }
    }

    /* (non-Javadoc)
     * @see com.ibm.g11n.pipeline.resfilter.ResourceFilterProvider#getAvailableResourceFilters()
     */
    @Override
    public Iterator<FilterInfo> getAvailableResourceFilters() {
        return FILTERS.values().iterator();
    }

    /* (non-Javadoc)
     * @see com.ibm.g11n.pipeline.resfilter.ResourceFilterProvider#getResourceFilter(java.lang.String)
     */
    @Override
    public ResourceFilter getResourceFilter(String id) {
        id = id.toUpperCase(Locale.ROOT);
        if (!FILTERS.containsKey(id)) {
            return null;
        }

        ResourceFilter result = null;
        switch (Filter.valueOf(id)) {
        case AMDJS:
            result = new AmdJsResource();
            break;
        case ANDROID:
            result = new AndroidStringsResource();
            break;
        case GLOBALIZEJS:
            result = new GlobalizeJsResource();
            break;
        case IOS:
            result = new IOSStringsResource();
            break;
        case JAVA:
            result = new JavaPropertiesResource(Encoding.ISO_8859_1, MessagePatternEscape.AUTO);
            break;
        case JAVAMSG:
            result = new JavaPropertiesResource(Encoding.ISO_8859_1, MessagePatternEscape.ALL);
            break;
        case JAVAMSGUTF8:
            result = new JavaPropertiesResource(Encoding.UTF_8, MessagePatternEscape.ALL);
            break;
        case JAVAUTF8:
            result = new JavaPropertiesResource(Encoding.UTF_8, MessagePatternEscape.AUTO);
            break;
        case JSON:
            result = new JsonResource();
            break;
        case PO:
            result = new POResource();
            break;
        case POT:
            result = new POTResource();
            break;
        case XLIFF:
            result = new XLIFFResource();
            break;
        case YML:
            result = new YMLResource();
            break;
        }

        return result;
    }

    /* (non-Javadoc)
     * @see com.ibm.g11n.pipeline.resfilter.ResourceFilterProvider#getMultiBundleResourceFilter(java.lang.String)
     */
    @Override
    public MultiBundleResourceFilter getMultiBundleResourceFilter(String id) {
        // no multi bundle resource filters available yet
        return null;
    }

    // TODO: GP Maven/Ant plugin uses different path-bundle name mapping for Java.
    // This method is used for checking if the type is one of Java types.
    // We probably should provide path-bundle name mapping "function" as a part
    // of ResourceFilter/MultiBundleResourceFilter interface instead. Revisit this later.
    public static boolean isJavaType(String type) {
        type = type.toUpperCase(Locale.ROOT);
        return type.equals(Filter.JAVA.name()) || type.equals(Filter.JAVAUTF8.name())
                || type.equals(Filter.JAVAMSG.name()) || type.equals(Filter.JAVAMSGUTF8.name());
    }
}
