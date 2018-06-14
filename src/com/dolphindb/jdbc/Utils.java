package com.dolphindb.jdbc;

import com.xxdb.data.*;

import java.sql.Date;
import java.sql.SQLException;
import java.sql.Time;
import java.sql.Timestamp;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Utils {
    public static Object java2db(Object o){
        System.out.println(o instanceof Entity);
        if(o instanceof BasicStringVector || o instanceof BasicAnyVector || o instanceof AbstractVector || o instanceof Vector){
            String s = ((Vector)o).getString();
            if(((Vector) o).get(0) instanceof BasicString){
                return dbVectorString(s);
            }else{
                return s;
            }
        }else if(o instanceof String || o instanceof BasicString){
            return "\""+o+"\"";
        }else if(o instanceof Character){
            return "'"+ o +"'";
        }else if(o instanceof Short || o instanceof BasicShort){
            return o + "h";
        }else if(o instanceof Float || o instanceof BasicFloat){
            return o + "f";
        }else if(o instanceof Date){
            return new BasicDate(((Date) o).toLocalDate());
        }else if(o instanceof Time){
            return new BasicTime(((Time) o).toLocalTime());
        }else if(o instanceof Timestamp){
            return new BasicTimestamp(((Timestamp) o).toLocalDateTime());
        }else if(o instanceof YearMonth){
            return new BasicMonth((YearMonth)o);
        }else {
            return  o;
        }

    }

    public static String dbVectorString(String s){
        String[] strings = s.substring(1,s.length()-1).split(",");
        StringBuilder sb = new StringBuilder("(");
        for(String it : strings){
            sb.append("`").append(it).append(",");
        }
        sb.delete(sb.length()-1,sb.length());
        sb.append(")");
        return sb.toString();
    }

    public static void joinOrder(StringBuilder sb,String[] values,String join){
        for(String item : values){
            if (item != null && item.length()>0){
                sb.append(item).append(join);
            }else{
                break;
            }
        }
        sb.delete(sb.length() - join.length(),sb.length());
    }

    public static void parseProperties(String s, Properties prop, String split1, String split2) throws SQLException {
        System.out.println(s);
        String[] strings1 = s.split(split1);
        String[] strings2;
        for (String item : strings1){
            if(item.length()>0) {
                strings2 = item.split(split2);
                if(strings2.length == 2) {
                    if(strings2[0].length()==0) throw new SQLException(item + "     is error");
                    prop.setProperty(strings2[0], strings2[1]);
                }else {
                    throw new SQLException(item + "     is error");
                }
            }
        }
    }

    public static String[] getProperties(Properties prop,String[] keys){
        String[] properties = new String[keys.length];
        int index = 0;
        for(String key : keys){
            properties[index] = prop.getProperty(key);
            index++;
        }
        return properties;
    }

    public static boolean isInsert(String sql){
        if(sql.startsWith("insert") || sql.startsWith("tableInsert")){
            return true;
        }else{
            return false;
        }
    }

    public static String getTableName(String sql){
        String tableName = null;
        if(sql.startsWith("insert")){
            tableName = sql.substring(sql.indexOf("into") + "into".length(), sql.indexOf("values"));
        }else if(sql.startsWith("tableInsert")){
            tableName = sql.substring(sql.indexOf("(") + "(".length(), sql.indexOf(","));
        }else if(sql.startsWith("append!")){
            tableName = sql.substring(sql.indexOf("(") + "(".length(), sql.indexOf(","));
        }else if(sql.contains(".append!")){
            tableName = sql.split("\\.")[0];
        }else if(sql.startsWith("update")){
            tableName = sql.substring(sql.indexOf("update") + "update".length(), sql.indexOf("set"));
        }else if(sql.contains(".update!")){
            tableName = sql.split("\\.")[0];
        }else if(sql.startsWith("delete")){
            int index = sql.indexOf("where");
            if(index != -1) {
                tableName = sql.substring(sql.indexOf("from") + "from".length(), sql.indexOf("where"));
            }else{
                tableName = sql.substring(sql.indexOf("from") + "from".length()).replaceAll(";","");
            }
        }
        return tableName;
    }

    public static boolean isUpdateable(String s){
        String s1 = s.trim().split(";")[0];
        String regex = "full.*join|inner.*join|right.*join|left.*join|" +
                "join.*(.*)|ej.*(.*)|sej.*(.*)|lj.*(.*)|fj.*(.*)|aj.*(.*)|cj.*(.*)|" +
                "group.*by|context.*by|pivot.*by";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(s1);

        return !matcher.find();
    }

    public static String getRandomString(int length) {
        String character = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
        String base = character + "0123456789";
        Random random = new Random();
        StringBuffer sb = new StringBuffer();
        int number = random.nextInt(character.length());
        sb.append(character.charAt(number));
        for (int i = 1; i < length; i++) {
            number = random.nextInt(base.length());
            sb.append(base.charAt(number));
        }
        return sb.toString();
    }

    public static BasicTable Vevtor2Table(Vector vector,String sql) {
        List<String> colNames = new ArrayList<>(1);
        sql = sql.trim();
        if (sql.contains("as")) {
            colNames.add(sql.split(" ")[3]);
        } else {
            colNames.add(sql.split(" ")[1]);
        }
        List<Vector> cols = new ArrayList<>(1);
        cols.add(vector);
        return new BasicTable(colNames, cols);
    }

}
