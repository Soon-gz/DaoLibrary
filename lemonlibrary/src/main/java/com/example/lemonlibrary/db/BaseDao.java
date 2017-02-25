package com.example.lemonlibrary.db;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;
import android.util.Log;

import com.example.lemonlibrary.db.annotion.DbField;
import com.example.lemonlibrary.db.annotion.DbTable;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Created by ShuWen on 2017/2/9.
 */

public abstract class BaseDao<T> implements IBaseDao<T> {
    private SQLiteDatabase database;
    private HashMap<String,Field> cacheMap;
    private String tableName;
    private Class<T> entityClazz;
    private boolean isInit = false;

    protected synchronized boolean init(Class<T> entity,SQLiteDatabase sqLiteDatabase){
        if (!isInit){
            this.database = sqLiteDatabase;
            this.entityClazz = entity;
            if (entityClazz.getAnnotation(DbTable.class) != null){
                tableName = entityClazz.getAnnotation(DbTable.class).value();
            }else {
                tableName = entity.getSimpleName();
            }
            if (!sqLiteDatabase.isOpen()){
                return false;
            }
            if (!TextUtils.isEmpty(createTable(entity))){
                database.execSQL(createTable(entity));
            }
            cacheMap = new HashMap<>();
            initCacheMap();

        }
        return isInit;
    }

    protected String createTable(Class<T> entity){
        Field[]fields = entity.getDeclaredFields();
        String tableName;
        if (entity.getAnnotation(DbTable.class) != null){
            tableName = ((DbTable)entity.getAnnotation(DbTable.class)).value();
        }else {
            tableName = entity.getSimpleName();
        }
        List<Field> aFileds = new ArrayList<>();
        for (Field field:fields) {
            if (field.getAnnotation(DbField.class) != null){
                aFileds.add(field);
            }
        }

        List<String> fieldNames = new ArrayList<>();
        List<String> fieldTypes = new ArrayList<>();
        for (int i = 0;i < aFileds.size() ;i++) {
            String fieldName;
            String fieldType;
            if (aFileds.get(i).getAnnotation(DbField.class) != null){
                fieldName = aFileds.get(i).getAnnotation(DbField.class).value();
                Class type = aFileds.get(i).getType();
                if(type==String.class){
                    fieldType = "TEXT";
                }else if(type==Double.class || type == double.class){
                    fieldType = "REAL";
                }else if(type== Integer.class|| type == int.class){
                    fieldType = "INTEGER";
                }else if(type == Long.class|| type == long.class){
                    fieldType = "LONG";
                }else if(type == byte[].class){
                    fieldType = "BLOB";
                }else if (type == boolean.class || type == Boolean.class){
                    fieldType = "Boolean";
                    /**
                     * 不支持的类型
                     */
                }else if (type == short.class || type == Short.class){
                    fieldType = "short";
                }else {
                    fieldType = "varchar(20)";
                }
            }else {
                fieldType = "varchar(20)";
                fieldName = aFileds.get(i).getName();
            }
            fieldTypes.add(fieldType);
            fieldNames.add(fieldName);
        }
        StringBuilder builder = new StringBuilder();
        builder.append("create table if not exists " + tableName +"(");
        for (int index = 0; index < fieldNames.size(); index++) {

            builder.append(fieldNames.get(index) +" "+ fieldTypes.get(index));
            if (index != fieldNames.size() - 1){
                builder.append(",");
            }else {
                builder.append(")");
            }
        }
        return builder.toString();
    }

    @Override
    public List<T> query(String sql) {
        return null;
    }

    private void initCacheMap() {
        String sql = "select * from "+tableName+" limit 0,1";
        Cursor cursor = null;
        try{
            cursor = database.rawQuery(sql,null);
            String[]colNames = cursor.getColumnNames();
            Field[]fields = entityClazz.getDeclaredFields();
            for (Field field:fields) {
                field.setAccessible(true);
            }
            for (String colName:colNames) {
                Field colField = null;
                for (Field field:fields) {
                    String fieldName;
                    if (field.getAnnotation(DbField.class) != null){
                        fieldName = field.getAnnotation(DbField.class).value();
                    }else {
                        fieldName = field.getName();
                    }

                    if (fieldName.equals(colName)){
                        colField = field;
                        break;
                    }
                }

                if (colField != null){
                    cacheMap.put(colName,colField);
                }
            }
        }catch (Exception e){

        }finally {
            cursor.close();
        }
    }

    @Override
    public Long insert(T entity) {
        HashMap<String,String>map = getValues(entity);
        ContentValues contentValues = getContentValues(map);
        Long result = database.insert(tableName,null,contentValues);
        return result;
    }

    @Override
    public int update(T newEntity, T where) {

        HashMap<String,String> whereMap = getConditionValues(where);

        HashMap<String,String> newEntityMap = getConditionValues(newEntity);

        ContentValues contentValues = getContentValues(newEntityMap);

        Condition condition = new Condition(whereMap);

        int result = database.update(tableName,contentValues,condition.getWhereCause(),condition.getWhereCauseArry());

        return result;
    }

    @Override
    public int delete(T entity) {

        HashMap<String,String> whereMap = getConditionValues(entity);

        Condition condition = new Condition(whereMap);

        int result = database.delete(tableName,condition.getWhereCause(),condition.getWhereCauseArry());
        return result;
    }

    @Override
    public List<T> query(T entity) {
        return query(entity,null,null,null);
    }

    @Override
    public List<T> query(T entity,String orderby,Integer startInt,Integer limit ) {

        HashMap<String,String> whereMap = getConditionValues(entity);
        String lintStr = null;
        if (startInt != null && limit != null){
            lintStr  = startInt+" , "+limit;
        }

        Condition condition = new Condition(whereMap);

        Cursor cursor = database.query(tableName,null,condition.getWhereCause(),condition.getWhereCauseArry(),null,null,orderby,lintStr);

        List<T> result = getResult(cursor,entity);

        return result;
    }

    private List<T> getResult(Cursor cursor, T entity) {
        List result = new ArrayList();
        Object item;
        while (cursor.moveToNext()){
            try {
                item = entity.getClass().newInstance();
                Iterator<Map.Entry<String,Field>> colNameIterator = cacheMap.entrySet().iterator();
                while (colNameIterator.hasNext()){
                    Map.Entry<String,Field> entry = colNameIterator.next();
                    String colName = entry.getKey();
                    int colIndex = cursor.getColumnIndex(colName);
                    Field colField = entry.getValue();
                    Class type = colField.getType();
                    if(colIndex!=-1){
                        if(type==String.class){
                            colField.set(item,cursor.getString(colIndex));
                        }else if(type==Double.class || type == double.class){
                            colField.set(item,cursor.getDouble(colIndex));
                        }else if(type== Integer.class || type == int.class){
                            int value =cursor.getInt(colIndex);
                            Log.i("dongnao","value="+value);
                            colField.set(item,cursor.getInt(colIndex));
                        }else if(type == Long.class || type == long.class){
                            colField.set(item,cursor.getLong(colIndex));
                        }else if(type == byte[].class){
                            colField.set(item,cursor.getBlob(colIndex));
                        }else if (type == boolean.class || type == Boolean.class){
                            String boolStr = cursor.getString(colIndex);
                            boolean b= false;
                            if ("true".equals(boolStr)){
                                b = true;
                            }
                            colField.set(item,b);
                        }else if (type == short.class || type == Short.class){
                            colField.set(item,cursor.getShort(colIndex));
                        }else {
                            /**
                             * 无法识别类型
                             */
                        }
                    }

                }
                result.add(item);
            } catch (InstantiationException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        return result;
    }

    private ContentValues getContentValues(HashMap<String, String> map) {
        ContentValues contentValues = new ContentValues();
        Iterator<String>keyIterator = map.keySet().iterator();
        while (keyIterator.hasNext()){
            String cacheKey = keyIterator.next();
            String cacheValue = map.get(cacheKey);
            if (cacheValue != null){
                contentValues.put(cacheKey,cacheValue);
            }
        }
        return contentValues;
    }

    private HashMap<String,String> getValues(T entity){
        HashMap<String,String> map = new HashMap<>();
        Iterator<Field> valueIterator = cacheMap.values().iterator();
        while (valueIterator.hasNext()){
            String cacheKey;
            String cacheValue;
            Field colField = valueIterator.next();
            if (colField.getAnnotation(DbField.class) != null){
                cacheKey = colField.getAnnotation(DbField.class).value();
            }else {
                cacheKey = colField.getName();
            }
            try {
                if (null == colField.get(entity)){
                    continue;
                }
                cacheValue = colField.get(entity).toString();
                map.put(cacheKey,cacheValue);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        return map;
    }
    private HashMap<String,String> getConditionValues(T entity){
        HashMap<String,String> map = new HashMap<>();
        Iterator<Field> valueIterator = cacheMap.values().iterator();
        while (valueIterator.hasNext()){
            String cacheKey;
            String cacheValue;
            Field colField = valueIterator.next();
            if (colField.getAnnotation(DbField.class) != null){
                cacheKey = colField.getAnnotation(DbField.class).value();
            }else {
                cacheKey = colField.getName();
            }
            try {
                if (null == colField.get(entity)){
                    continue;
                }
                cacheValue = colField.get(entity).toString();
                if ("0".equals(cacheValue) || "0.0".equals(cacheValue)){
                    continue;
                }
                map.put(cacheKey,cacheValue);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        return map;
    }

    private class Condition{
        private String whereCause;
        private String[] whereCauseArry;

        public Condition(HashMap<String,String> whereCache){
            Iterator<String> keyIterator = cacheMap.keySet().iterator();
            List<String> whereValues = new ArrayList<>();
            StringBuilder builder = new StringBuilder(" 1=1 ");
            while (keyIterator.hasNext()){
                String key = keyIterator.next();

                String whereValue = whereCache.get(key);
                if (whereValue != null ){
                    builder.append("and "+key+" =? ");
                    whereValues.add(whereValue);
                }
            }
            whereCause = builder.toString();
            whereCauseArry = whereValues.toArray(new String[whereValues.size()]);
        }

        public String getWhereCause() {
            return whereCause;
        }

        public void setWhereCause(String whereCause) {
            this.whereCause = whereCause;
        }

        public String[] getWhereCauseArry() {
            return whereCauseArry;
        }

        public void setWhereCauseArry(String[] whereCauseArry) {
            this.whereCauseArry = whereCauseArry;
        }
    }
}
