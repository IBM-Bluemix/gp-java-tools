/*
 * Copyright IBM Corp. 2015, 2018
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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.ibm.g11n.pipeline.resfilter.FilterOptions;
import com.ibm.g11n.pipeline.resfilter.LanguageBundle;
import com.ibm.g11n.pipeline.resfilter.LanguageBundleBuilder;
import com.ibm.g11n.pipeline.resfilter.ResourceFilter;
import com.ibm.g11n.pipeline.resfilter.ResourceFilterException;
import com.ibm.g11n.pipeline.resfilter.ResourceString;

public class YMLResource extends ResourceFilter {

    // Take a yml file and converts a flattened map object to upload
    // to Globalization Pipeline service
    @Override
    public LanguageBundle parse(InputStream inStream, FilterOptions options)
            throws IOException, ResourceFilterException {

        char separator = '.';

        YAMLFactory yf = new YAMLFactory();
        ObjectMapper mapper = new ObjectMapper(yf);
        Map<String, Object> YAML_map = new LinkedHashMap<String, Object>();

        // Reads contents of YAML file and converts it to a hashmap
        YAML_map = mapper.readValue(inStream, new TypeReference<LinkedHashMap<String, Object>>() {
        });

        Map<String, String> resultMap = flattenMap("", YAML_map, new LinkedHashMap<String, String>(), separator);

        LanguageBundleBuilder bb = new LanguageBundleBuilder(true);
        for (Entry<String, String> entry : resultMap.entrySet()) {
            bb.addResourceString(entry.getKey(), entry.getValue());
        }
        return bb.build();
    }

    // flattens map object from {a={b=c}} to {a<sep>b=c}
    @SuppressWarnings("unchecked")
    private Map<String, String> flattenMap(String prefix, Map<String, Object> map, Map<String, String> resource,
            char separator) {

        for (Object key : map.keySet()) {
            String key_str = (String) key;
            Object value = map.get(key);

            // The only possible types for the KVP's are string (final value) or
            // maps (intermediate)
            boolean isString = value.getClass().equals(String.class);
            boolean isMap = value.getClass().equals(LinkedHashMap.class);

            StringBuilder new_prefix = new StringBuilder(100);
            new_prefix.append(prefix).append(key_str).append(separator);

            if (isString) {
                // Gets rid of last character since we always append the dot at
                // the end
                resource.put(new_prefix.substring(0, new_prefix.length() - 1), (String) value);
            }

            else if (isMap) {
                flattenMap(new_prefix.toString(), (Map<String, Object>) value, resource, separator);
            }
        }

        return resource;
    }

    // Takes a flattened hashmap and creates a yml file
    @SuppressWarnings("unchecked")
    @Override
    public void write(OutputStream outStream, LanguageBundle languageBundle,
            FilterOptions options) throws IOException, ResourceFilterException {

        List<ResourceString> resStrings = languageBundle.getSortedResourceStrings();

        // First we need to unflatten the map before writing the file
        char separator = '.';

        // map that we will alter in order to populate
        HashMap<String, Object> temp_map = new HashMap<String, Object>();

        // map that points to the top so we always have a reference
        HashMap<String, Object> new_map = temp_map;

        for (ResourceString res : resStrings) {
            String value = res.getValue();
            String key = res.getKey();
            String[] temp = key.split("\\" + separator);

            for (int i = 0; i < temp.length; i++) {

                // last intermediate key - insert value
                if (i == temp.length - 1) {
                    temp_map.put(temp[i], value);
                }

                // still in intermediary key - insert a new map
                else {
                    if (!temp_map.containsKey(temp[i])) {
                        temp_map.put(temp[i], new HashMap<String, Object>());
                    }
                    temp_map = (HashMap<String, Object>) temp_map.get(temp[i]);
                }
            }

            // reset map to point to top of HashMap
            temp_map = new_map;
        }

        // Actually write out the file
        writeFile(outStream, temp_map, 0);
    }

    // Helper function to actually write the file
    // Assume the difference between depth n and depth n+1 is 1 whitespace
    @SuppressWarnings("unchecked")
    private void writeFile(OutputStream writer, Map<String, Object> map, int depth) throws IOException {
        StringBuilder whiteSpace = new StringBuilder(100);
        StringBuilder toWrite = new StringBuilder(100);

        for (int i = 0; i < depth; i++) {
            whiteSpace.append(" ");
        }

        for (Object key : map.keySet()) {
            toWrite.append(whiteSpace).append((String) key).append(":");
            Object value = map.get(key);

            boolean isString = value.getClass().equals(String.class);
            boolean isMap = value.getClass().equals(HashMap.class);

            // only append the value if we know type of value is a string and
            // append the new line character
            toWrite.append((isString) ? new StringBuilder((" \"")).append((String) value).append("\"") : "")
                    .append("\n");
            writer.write(toWrite.toString().getBytes(Charset.forName("UTF-8")));

            if (isMap) {
                Map<String, Object> valueMap = (Map<String, Object>) value;
                writeFile(writer, valueMap, depth + 1);
            }

            toWrite.replace(0, toWrite.length(), "");
        }
        return;
    }

    // TODO: Implement merge method
    //
    // public void merge(InputStream baseStream, OutputStream outStream, LanguageBundle languageBundle,
    //        Locale textProcessingLocale) throws IOException, ResourceFilterException
}
