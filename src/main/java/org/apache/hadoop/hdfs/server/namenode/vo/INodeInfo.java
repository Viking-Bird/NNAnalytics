package org.apache.hadoop.hdfs.server.namenode.vo;

import org.apache.commons.lang.time.DateFormatUtils;

import java.io.Serializable;
import java.util.Date;

/**
 * 用于前后端传输用的INode
 *
 * @author pengwang
 * @date 2019/07/11
 */
public class INodeInfo implements Serializable {

    private static final String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";

    public Long nodeId;
    public String path;
    private String permisssion;
    private String accessTime;
    private String modTime;
    private Long nsQuota;
    private Long dsQuota;
    private String xAttrs;
    private Integer aclsCount;
    private Integer type;
    private Boolean isUnderConstruction;
    private Boolean isWithSnapshot;
    private Boolean isSnapshottable;
    private Long fileSize;
    private Short replicationFactor;
    private Long numBlocks;
    private String userName;
    private String groupName;

    public Long getNodeId() {
        return nodeId;
    }

    public void setNodeId(Long nodeId) {
        this.nodeId = nodeId;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getPermisssion() {
        return permisssion;
    }

    public void setPermisssion(String permisssion) {
        this.permisssion = permisssion;
    }

    public String getAccessTime() {
        return accessTime;
    }

    public void setAccessTime(Long accessTime) {
        this.accessTime = DateFormatUtils.format(new Date(accessTime),DATE_FORMAT);;
    }

    public String getModTime() {
        return modTime;
    }

    public void setModTime(Long modTime) {
        this.modTime = DateFormatUtils.format(new Date(modTime),DATE_FORMAT);
    }

    public Long getNsQuota() {
        return nsQuota;
    }

    public void setNsQuota(Long nsQuota) {
        this.nsQuota = nsQuota;
    }

    public Long getDsQuota() {
        return dsQuota;
    }

    public void setDsQuota(Long dsQuota) {
        this.dsQuota = dsQuota;
    }

    public String getxAttrs() {
        return xAttrs;
    }

    public void setxAttrs(String xAttrs) {
        this.xAttrs = xAttrs;
    }

    public Integer getAclsCount() {
        return aclsCount;
    }

    public void setAclsCount(Integer aclsCount) {
        this.aclsCount = aclsCount;
    }

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    public Boolean getUnderConstruction() {
        return isUnderConstruction;
    }

    public void setUnderConstruction(Boolean underConstruction) {
        isUnderConstruction = underConstruction;
    }

    public Boolean getWithSnapshot() {
        return isWithSnapshot;
    }

    public void setWithSnapshot(Boolean withSnapshot) {
        isWithSnapshot = withSnapshot;
    }

    public Boolean getSnapshottable() {
        return isSnapshottable;
    }

    public void setSnapshottable(Boolean snapshottable) {
        isSnapshottable = snapshottable;
    }

    public Long getFileSize() {
        return fileSize;
    }

    public void setFileSize(Long fileSize) {
        this.fileSize = fileSize;
    }

    public Short getReplicationFactor() {
        return replicationFactor;
    }

    public void setReplicationFactor(Short replicationFactor) {
        this.replicationFactor = replicationFactor;
    }

    public Long getNumBlocks() {
        return numBlocks;
    }

    public void setNumBlocks(Long numBlocks) {
        this.numBlocks = numBlocks;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }
}