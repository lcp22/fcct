package cn.lcp.fcct.util;

import cn.lcp.fcct.po.InformationSchema;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;

import java.io.*;
import java.util.*;

public class CodeFactory {
    public static CodeFactory instance;
    private static Boolean FLAG ;
    private static String code_package ;
    private static List<String> codeTemplates = new ArrayList<>();
    private static List<String> files = new ArrayList<>();
    private static Boolean excludeFirstParamater;
    private static List<String> excludefiles = new ArrayList<>();
    private static List<String> exclude_fileds = new ArrayList<>();

    private CodeFactory(){}
    public static CodeFactory getInstance(){
        if(instance == null){
            instance = new CodeFactory();
            try {
                Properties properties = new Properties();
                FileInputStream in = new FileInputStream("CodeTemplates/CodeTemplates.properties");
                properties.load(in);
                in.close();
                FLAG = Boolean.parseBoolean(properties.getProperty("COVER_FLAG").trim());
                code_package = properties.getProperty("CODE_PACKAGE").trim();
                excludeFirstParamater = Boolean.parseBoolean(properties.getProperty("EXCLUDE_FIRST_PARAMATER").trim());
                for (String exclude_file:properties.getProperty("EXCLUDE_FILES").trim().split(",")) {
                    excludefiles.add(exclude_file);
                }
                for (String exclude_filed:properties.getProperty("EXCLUDE_FILEDS").trim().split(",")) {
                    exclude_fileds.add(exclude_filed);
                }
                for (String code_templates_folder:properties.getProperty("TEMPLATES").trim().split(",")) {
                    switch (code_templates_folder){
                        case "po":
                            codeTemplates.add("Po.java");
                            files.add(code_package+"po/");
                            break;
                        case "dao":
                            codeTemplates.add("Dao.java");
                            files.add(code_package+"dao/");
                            break;
                        case "server":
                            codeTemplates.add("Service.java");
                            files.add(code_package+"server/");
                            break;
                        case "serverImpl":
                            codeTemplates.add("ServiceImpl.java");
                            files.add(code_package+"server/Impl/");
                            break;
                    }
                }
                for (String code_template:properties.getProperty("EXCLUDE_FILES").trim().split(",")) {
                    excludefiles.add(code_template);
                }
            }catch (IOException e){
                throw new RuntimeException("配置加载错误,请确认路径或参数是否正确!");
            }

        }
        return instance;
    }

    private Boolean excludeParamater(Integer index,String paramater){
        if(excludeFirstParamater&&index == 0){
            return false;
        }
        for (String excludefile:excludefiles) {
            if(excludefile.equals(paramater)){
                return false;
            }
        }
        return true;
    }

    private Boolean validateParamateNum(List<InformationSchema> informationSchemas,List<String> exclude_fileds){
        List<String> vpn = new ArrayList<>();
        for (int i = 0; i < informationSchemas.size(); i++) {
            if(excludeFirstParamater&&i==0){
                continue;
            }
            String columnName = informationSchemas.get(i).getColumnName();
            if(exclude_fileds.contains(columnName)){
                vpn.add(columnName);
            }
        }
        return vpn.size()!=1;
    }

    private String formatSqlParamater(String sqlParamater){
        String paramater = "";
        switch (sqlParamater){
            case "int":
                paramater = "Integer";
                break;
            case "bigint":
                paramater = "Long";
                break;
            case "varchar":
                paramater = "String";
                break;
            case "datetime":
                paramater = "Date";
                break;
            case "timestamp":
                paramater = "Date";
                break;
        }
        return paramater;
    }

    /**
     *
     * @param tableName 数据库表名
     * @param informationSchemas 表内相关属性
     *         columnName:列名 dataType:列类型 columnComment:列描述
     * @throws Exception
     */
    public void CreateCode(String tableName,List<InformationSchema> informationSchemas){
        if (codeTemplates.size() != files.size()) {
            throw new RuntimeException("templates.length != files.length");
        }
        VelocityContext context = new VelocityContext();
        Map<String, String> entity = getEntity(tableName,informationSchemas);
        System.out.println(entity.get("insertEntity"));
        String conversionTableName = entity.get("poEntity");
        context.put("insertEntity",entity.get("insertEntity"));
        context.put("deleteEntity",entity.get("deleteEntity"));
        context.put("selectEntity",entity.get("selectEntity"));
        context.put("getEntity",entity.get("getEntity"));
        context.put("updateEntity",entity.get("updateEntity"));
        context.put("poEntity",conversionTableName);
        context.put("poParamaterEntity",entity.get("poParamaterEntity"));
        context.put("poParamaterGetSetEntity",entity.get("poParamaterGetSetEntity"));
        context.put("serviceEntity",entity.get("serviceEntity"));
        context.put("serviceImplEntity",entity.get("serviceImplEntity"));
        context.put("creatTime",new Date().toString());
        try{
            for (int i = 0; i < files.size(); i++) {
                Template template = Velocity.getTemplate("/CodeTemplates/"+codeTemplates.get(i), "UTF-8");
                File file = null;
                if ("Po.java".equals(codeTemplates.get(i))) {
                    file = new File(files.get(i) + conversionTableName + codeTemplates.get(i).substring(2));
                }else {
                    file = new File(files.get(i) + conversionTableName + codeTemplates.get(i));
                }
                File parentFile = file.getParentFile();
                if (!parentFile.exists()) {
                    parentFile.mkdirs();
                }
                if (!FLAG && file.exists()) {
                    continue;
                }
                System.out.println(file.getAbsolutePath());
                FileWriter fileWriter = new FileWriter(file);
                template.merge(context, fileWriter);
                fileWriter.close();
            }
        }  catch (Exception e){
            e.printStackTrace();
        }
    }

    private Map<String,String> getEntity(String tableName,List<InformationSchema> informationSchemas){
        Map<String,String> map = new HashMap<>();
        StringBuilder poParamaterEntity = new StringBuilder();
        StringBuilder poParamaterGetSetEntity = new StringBuilder();
        StringBuilder insertEntity = new StringBuilder();
        StringBuilder insertEntity2 = new StringBuilder();
        StringBuilder updateEntity = new StringBuilder();
        StringBuilder deleteEntity = new StringBuilder();
        StringBuilder selectEntity = new StringBuilder();
        StringBuilder getEntity = new StringBuilder();
        StringBuilder serviceEntity = new StringBuilder();
        StringBuilder serviceImplEntity = new StringBuilder();
        String[] ts = tableName.split("_");
        StringBuilder conversionTableName = new StringBuilder();
        for (String t:ts) {
            conversionTableName.append(t.substring(0,1).toUpperCase()+t.substring(1));
        }
        //Dao
        insertEntity.append("    @Insert(\"insert into "+tableName+"(");
        insertEntity2.append(" values(");
        String poIDName = "";
        String tableIDName = "";
        String poIDType = "";
        StringBuilder setSQL = new StringBuilder();
        setSQL.append("");
        Boolean vpn = validateParamateNum(informationSchemas, exclude_fileds);
        for (int i = 0; i < informationSchemas.size(); i++) {
            StringBuilder columnName = new StringBuilder();
            String cn = informationSchemas.get(i).getColumnName();
            String[] cs = cn.split("_");
            for (int j = 0; j < cs.length; j++) {
                if(j==0){
                    columnName.append(cs[j]);
                }else{
                    columnName.append(cs[j].substring(0,1).toUpperCase()).append(cs[j].substring(1));
                }
            }
            String poDataType = formatSqlParamater(informationSchemas.get(i).getDataType());
            if(i==0){
                poIDName = columnName.toString();
                tableIDName = cn;
                poIDType = poDataType;
            }
            //Po
            if(!exclude_fileds.contains(informationSchemas.get(i).getColumnName())){
                poParamaterEntity.append("    private ").append(poDataType+" ").
                        append(columnName+";     //").append(informationSchemas.get(i).getColumnComment()+"\n");
                poParamaterGetSetEntity.append("    public ").append(poDataType+" ").
                        append("get"+columnName.substring(0,1).toUpperCase()+columnName.substring(1)+"() {\n        return ").
                        append(columnName+";\n    }").append("\n    public "+conversionTableName+" ").
                        append("set"+columnName.substring(0,1).toUpperCase()+columnName.substring(1)+"(").
                        append(poDataType+" ").
                        append(columnName+") {\n").append("        this."+columnName+" = "+columnName+";\n        return this;\n    }\n");
            }
            if(excludeParamater(i,cn)){
                String conversionTableNameLower = conversionTableName.toString().substring(0,1).toLowerCase()+conversionTableName.toString().substring(1);
                setSQL.append("        if("+conversionTableNameLower+".get"+columnName.toString().substring(0,1).toUpperCase()+columnName.toString().substring(1)+"()!=null){\n            updatesql.append(\""+cn+"=#{"+columnName+"} and \");\n        }\n");
                if(!vpn||informationSchemas.size()==i+1){
                    insertEntity2.append("#{"+columnName+"})\")\n");
                    insertEntity.append(cn+") ");
                    insertEntity.append(insertEntity2);
                    insertEntity.append("    @Options(useGeneratedKeys=true, keyProperty=\""+poIDName+"\", keyColumn=\""+tableIDName+"\")\n").
                            append("    int insert("+conversionTableName+" "+conversionTableNameLower+") throws  Exception;\n");
                    deleteEntity.append("    @Update(\"update  "+tableName+" set is_status=1 where "+tableIDName+"=#{"+poIDName+"}\")\n").
                            append("    int deleteById(@Param(\""+poIDName+"\") "+poIDType+" "+poIDName+") throws Exception;\n");
                    selectEntity.append("    @Select(\"select * from "+tableName+" where is_status=0\")\n").
                            append("    List<"+conversionTableName+"> selectAll() throws Exception;\n");
                    getEntity.append("    @Select(\"select * from "+tableName+" where "+tableIDName+"=#{"+poIDName+"}  and is_status=0\")\n").
                            append("    "+conversionTableName+" getById(@Param(\""+poIDName+"\") "+poIDType+" "+poIDName+") throws Exception;\n");
                    updateEntity.append("    @SelectProvider(type="+conversionTableName+"ServiceImpl.class,method=\"updateSql\")\n" +
                            "    int update("+conversionTableName+" "+conversionTableNameLower+");\n");
                    serviceEntity.append("    int insert("+conversionTableName+" "+conversionTableNameLower+");\n").
                            append("    int update("+conversionTableName+" "+conversionTableNameLower+");\n").
                            append("    List<"+conversionTableName+"> selectAll();\n").
                            append("    "+conversionTableName+" getById("+poIDType+" "+poIDName+");\n").
                            append("    int deleteById("+poIDType+" "+poIDName+");\n").
                            append("    String updateSql("+conversionTableName+" "+conversionTableNameLower+");");
                    serviceImplEntity.append("        @Autowired\n        private "+conversionTableName+"Dao "+conversionTableNameLower+"Dao;\n\n").
                            append("        @Override\n        public int insert("+conversionTableName+" "+conversionTableNameLower+") {\n            Integer sql_status = null;\n            try {\n                sql_status = "+conversionTableNameLower+"Dao.insert("+conversionTableNameLower+");\n            }catch (Exception e){\n                e.printStackTrace();\n            }\n            return sql_status;\n            \n\t\t}\n\n").
                            append("        @Override\n        public int update("+conversionTableName+" "+conversionTableNameLower+") {\n            Integer sql_status = null;\n            try {\n                sql_status = "+conversionTableNameLower+"Dao.update("+conversionTableNameLower+");\n            }catch (Exception e){\n                e.printStackTrace();\n            }\n            return sql_status;\n            \n\t\t}\n\n").
                            append("        @Override\n        public List<"+conversionTableName+"> selectAll() {\n            List<"+conversionTableName+"> datas = null;\n            try {\n                datas = "+conversionTableNameLower+"Dao.selectAll();\n            }catch (Exception e){\n                e.printStackTrace();\n            }\n            return datas;\n            \n\t\t}\n\n").
                            append("        @Override\n        public "+conversionTableName+" getById("+poIDType+" "+poIDName+") {\n            "+conversionTableName+" data = null;\n            try {\n                data = "+conversionTableNameLower+"Dao.getById("+poIDName+");\n            }catch (Exception e){\n                e.printStackTrace();\n            }\n            return data;\n            \n\t\t}\n\n").
                            append("        @Override\n        public int deleteById("+poIDType+" "+poIDName+") {\n            Integer sql_status = null;\n            try {\n                sql_status = "+conversionTableNameLower+"Dao.deleteById("+poIDName+");\n            }catch (Exception e){\n                e.printStackTrace();\n            }\n            return sql_status;\n            \n\t\t}\n\n").
                            append("        @Override\n        public String updateSql("+conversionTableName+" "+conversionTableNameLower+  ") {\n        StringBuilder updatesql = new StringBuilder();\n        updatesql.append(\"update "+tableName+" set \");\n"+setSQL+"        updatesql.delete(updatesql.length()-5,updatesql.length());\n        updatesql.append(\" where "+tableIDName+"=#{"+poIDName+"}\");\n        return updatesql.toString();\n\t\t}");
                }else{
                    insertEntity2.append("#{"+cn+"},");
                    insertEntity.append(cn+",");
                }
            }
        }
        map.put("insertEntity",insertEntity.toString());
        map.put("updateEntity",updateEntity.toString());
        map.put("deleteEntity",deleteEntity.toString());
        map.put("serviceEntity",serviceEntity.toString());
        map.put("getEntity",getEntity.toString());
        map.put("serviceImplEntity",serviceImplEntity.toString());
        map.put("selectEntity",selectEntity.toString());
        map.put("poParamaterEntity",poParamaterEntity.toString());
        map.put("poParamaterGetSetEntity",poParamaterGetSetEntity.toString());
        map.put("poEntity",conversionTableName.toString());
        return  map;
    }

}
