/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.hadoop.hdfs.server.namenode.analytics;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Collection;
import java.util.function.Function;
import java.util.function.ToLongFunction;
import java.util.stream.Stream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.hadoop.fs.Path;
import org.apache.hadoop.hdfs.server.namenode.INode;
import org.apache.hadoop.hdfs.server.namenode.INodeDirectory;
import org.apache.hadoop.hdfs.server.namenode.NameNodeLoader;
import org.apache.hadoop.hdfs.server.namenode.queries.BaseQuery;
import org.apache.hadoop.io.IOUtils;
import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonGenerator;

/** NNA Utility class. */
public class Helper {

    /**
     * Get the title of the Y axis for a chart based on the sum.
     *
     * @param sum type of sum
     * @return string representing the y axis
     */
    public static String toYAxis(String sum) {
        switch (sum) {
            case "count":
                return "# of INodes";
            case "fileSize":
                return "Bytes (No Replication Factor)";
            case "diskspaceConsumed":
                return "Bytes (With Replication Factor)";
            case "numBlocks":
                return "# of Blocks (No Replication Factor)";
            case "blockSize":
                return "Block Size (No Replication Factor)";
            case "numReplicas":
                return "# of Replicas (Blocks * Replication Factor)";
            case "memoryConsumed":
                return "Bytes";
            case "dsQuota":
                return "Quota";
            case "nsQuota":
                return "Quota";
            case "dsQuotaUsed":
                return "Quota Bytes Used";
            case "nsQuotaUsed":
                return "Namespace Quota Used";
            case "dsQuotaRatioUsed":
                return "Usage Percentage";
            case "nsQuotaRatioUsed":
                return "Usage Percentage";
            default:
                throw new IllegalArgumentException(
                        "Could not determine sum type: " + sum + ".\nPlease check /sums for available sums.");
        }
    }

    /**
     * Get the title of a chart based on histogram type.
     *
     * @param histType the histogram type
     * @param sum the sum type
     * @return a String representing the title of the histogram
     */
    public static String toTitle(String histType, String sum) {
        return histType.toUpperCase() + " Histogram | " + sum.toUpperCase();
    }

    static String getTrackingUrl(HttpServletRequest req) {
        String requestUri = req.getRequestURI();
        String queryString = req.getQueryString();
        if (queryString == null) {
            return requestUri;
        }
        return requestUri + "?" + queryString;
    }

    /**
     * Utility method for performing filtering against NameNode.
     *
     * @param nameNodeLoader the NameNodeLoader
     * @param set whether set of files or dirs
     * @param filters the filter types
     * @param filterOps the filter operations
     * @param find find a min, max, or avg of inode fields
     * @return the inodes collection that passed the filter
     */
    public static Collection<INode> performFilters(
            NameNodeLoader nameNodeLoader,
            String set,
            String[] filters,
            String[] filterOps,
            String find) {
        Collection<INode> interim = performFilters(nameNodeLoader, set, filters, filterOps);
        return nameNodeLoader.getQueryEngine().findFilter(interim, find);
    }

    /**
     * Utility method for performing filtering against NameNode.
     *
     * @param nameNodeLoader the NameNodeLoader
     * @param set whether set of files or dirs
     * @param filters the filter types
     * @param filterOps the filter operations
     * @return the inodes collection that passed the filter
     */
    public static Collection<INode> performFilters(
            NameNodeLoader nameNodeLoader, String set, String[] filters, String[] filterOps) {
        Collection<INode> inodes = nameNodeLoader.getINodeSet(set);

        if (filters == null || filters.length == 0 || filterOps == null || filterOps.length == 0) {
            return inodes;
        }

        return nameNodeLoader.getQueryEngine().combinedFilter(inodes, filters, filterOps);
    }

    /**
     * Utility method for setting up filtering against NameNode.
     *
     * @param nameNodeLoader the NameNodeLoader
     * @param set whether set of files or dirs 标识是文件还是目录
     * @param filters the filter types 过滤的类型
     * @param filterOps the filter operations 过滤操作符和值
     * @return the inodes collection that passed the filter 过滤后的inodes集合
     */
    public static Stream<INode> setFilters(
            NameNodeLoader nameNodeLoader, String set, String[] filters, String[] filterOps) {
        // 获取set对应的INode集合
        Collection<INode> inodes = nameNodeLoader.getINodeSet(set);

        // 过滤条件为空，直接返回并行操作流
        if (filters == null || filters.length == 0 || filterOps == null || filterOps.length == 0) {
            return inodes.parallelStream();
        }

        // 执行过滤操作，返回过滤结果
        return nameNodeLoader.getQueryEngine().combinedFilterToStream(inodes, filters, filterOps);
    }

    public static <T> ToLongFunction<T> convertToLongFunction(Function<T, Long> function) {
        return function::apply;
    }

    /**
     * Write a set of enums out to HTTP Response as a JSON list.
     *
     * @param resp the http response
     * @param values the enums
     * @throws IOException if parsing or writing fails
     */
    public static void toJsonList(HttpServletResponse resp, Enum[]... values) throws IOException {
        JsonGenerator json =
                new JsonFactory().createJsonGenerator(resp.getWriter()).useDefaultPrettyPrinter();
        try {
            json.writeStartObject();
            for (int i = 0; i < values.length; i++) {
                Enum[] enumList = values[i];
                json.writeArrayFieldStart("Possibilities " + (i + 1));
                for (Enum value : enumList) {
                    if (value != null) {
                        json.writeStartObject();
                        json.writeStringField("Name", value.name());
                        json.writeEndObject();
                    }
                }
                json.writeEndArray();
            }
            json.writeEndObject();
        } finally {
            IOUtils.closeStream(json);
        }
    }

    /**
     * Return String representation of enums as a JSON list.
     *
     * @param values the enums
     * @return String representation of enums as a JSON list
     * @throws IOException if parsing or writing fails
     */
    public static String toJsonList(Enum[]... values) throws IOException {
        StringWriter sw = new StringWriter();
        JsonGenerator json = new JsonFactory().createJsonGenerator(sw).useDefaultPrettyPrinter();
        try {
            json.writeStartObject();
            for (int i = 0; i < values.length; i++) {
                Enum[] enumList = values[i];
                json.writeArrayFieldStart("Possibilities " + (i + 1));
                for (Enum value : enumList) {
                    if (value != null) {
                        json.writeStartObject();
                        json.writeStringField("Name", value.name());
                        json.writeEndObject();
                    }
                }
                json.writeEndArray();
            }
            json.writeEndObject();
        } finally {
            IOUtils.closeStream(json);
        }
        return sw.toString();
    }

    /**
     * Parse the set of filters from the URL.
     *
     * @param fullFilterStr the full url filter string
     * @return a set of strings representing the filters
     */
    public static String[] parseFilters(String fullFilterStr) {
        if (fullFilterStr != null && !fullFilterStr.isEmpty()) {
            String[] filterSplits = fullFilterStr.split(",");
            String[] filters = new String[filterSplits.length];// 保存filters结果
            for (int i = 0; i < filterSplits.length; i++) {
                String[] filterSplit = filterSplits[i].split(":");
                if (filterSplit.length != 3) {
                    throw new IllegalArgumentException(
                            "Incorrect filter argument format for: '"
                                    + filterSplits[i]
                                    + "'. Needs to be <filter>:<op>:<filed>.");
                }
                String filter = filterSplit[0];
                filters[i] = filter;
            }
            return filters;
        }
        return null;
    }

    /**
     * Parse the set of filter operations from the URL.
     *
     * @param fullFilterStr the full url filter string
     * @return a set of strings representing the filter operations
     */
    public static String[] parseFilterOps(String fullFilterStr) {
        if (fullFilterStr != null && !fullFilterStr.isEmpty()) {
            String[] filterOpSplits = fullFilterStr.split(",");
            String[] filterOps = new String[filterOpSplits.length]; // 保存filterOps结果：操作符和值
            for (int i = 0; i < filterOpSplits.length; i++) {
                String[] filterOpSplit = filterOpSplits[i].split(":");
                if (filterOpSplit.length != 3) {
                    throw new IllegalArgumentException(
                            "Incorrect filter argument format for: '"
                                    + filterOpSplits[i]
                                    + "'. Needs to be <filter>:<op>:<filed>.");
                }
                String filterOp = filterOpSplit[1];
                String filterOpField = filterOpSplit[2];
                filterOps[i] = String.join(":", filterOp, filterOpField);
            }
            return filterOps;
        }
        return null;
    }

    /**
     * Returns function that maps an inode to its parent directory down to a specific depth.
     *
     * @param dirDepth the depth of the parent to fetch
     * @return a function
     */
    public static Function<INode, String> getDirectoryAtDepthFunction(int dirDepth) {
        return node -> {
            try {
                INodeDirectory parent = node.getParent();
                int topParentDepth = new Path(parent.getFullPathName()).depth();
                if (topParentDepth < dirDepth) {
                    return "NO_MAPPING";
                }
                for (int parentTravs = topParentDepth; parentTravs > dirDepth; parentTravs--) {
                    parent = parent.getParent();
                }
                return parent.getFullPathName().intern();
            } catch (Exception e) {
                return "NO_MAPPING";
            }
        };
    }

    /**
     * Create the query object used for tracking user queries.
     *
     * @param raw the http request
     * @param userName the username who issued the query
     * @return the query object for tracking
     */
    public static BaseQuery createQuery(HttpServletRequest raw, String userName) {
        return new BaseQuery(Helper.getTrackingUrl(raw), userName);
    }
}
