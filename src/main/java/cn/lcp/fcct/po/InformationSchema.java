package cn.lcp.fcct.po;

/**
 * 模板生成辅助类
 */
public class InformationSchema {
    private String columnName;//列名

    private String dataType;//列类型

    private String columnComment;//列描述

    public String getColumnName() {
        return columnName;
    }

    public void setColumnName(String columnName) {
        this.columnName = columnName;
    }

    public String getDataType() {
        return dataType;
    }

    public void setDataType(String dataType) {
        this.dataType = dataType;
    }

    public String getColumnComment() {
        return columnComment;
    }

    public void setColumnComment(String columnComment) {
        this.columnComment = columnComment;
    }
}
