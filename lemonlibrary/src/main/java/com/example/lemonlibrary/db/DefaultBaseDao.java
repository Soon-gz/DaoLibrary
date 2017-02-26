package com.example.lemonlibrary.db;

import android.content.ContentValues;
import android.database.Cursor;
import android.util.Log;

import com.example.lemonlibrary.db.annotion.DbField;
import com.example.lemonlibrary.db.annotion.DbPrimaryField;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Created by ShuWen on 2017/2/26.
 */

@SuppressWarnings("unchecked")
public abstract class DefaultBaseDao<T> extends BaseDao<T> {

    private  static final String TAG = "LemonDaoLibrary";
    /**
     * @param sql 执行的sql语句
     * @return
     */
    @Override
    public List<T> query(String sql,Class<T> entity) {
        return query(sql,entity,null);
    }

    /**
     * @param sql 执行语句
     * @param args 对应的sql中的条件
     * @return
     */
    @Override
    public List<T> query(String sql,Class<T> entity, String[] args) {
        List<T> result = null;
        try{
            Cursor cursor = database.rawQuery(sql, args);
            result = getResult(cursor,entity);
        }catch (Exception e){
            Log.e(TAG,"数据库语句异常，请检查！");
        }
        return result;
    }

    /**
     * @param entity 插入的类型具体数据
     */
    @Override
    public Long insert(T entity) {
        HashMap<String, String> map = getValues(entity);
        ContentValues contentValues = getContentValues(map);
        Long result = database.insert(tableName, null, contentValues);
        return result;
    }

    /**
     * @param sql 插入的sql语句
     */
    @Override
    public void insert(String sql) {
        try{
            database.execSQL(sql);
        }catch (Exception e){
            Log.e(TAG,"数据库语句异常，请检查！");
        }
    }

    /**
     * @param newEntity  新的类型
     * @param where 更新的条件
     * @return
     */
    @Override
    public int update(T newEntity, T where) {

        HashMap<String, String> whereMap = getConditionValues(where);

        HashMap<String, String> newEntityMap = getConditionValues(newEntity);

        ContentValues contentValues = getContentValues(newEntityMap);

        Condition condition = new Condition(whereMap);

        int result = database.update(tableName, contentValues, condition.getWhereCause(), condition.getWhereCauseArry());

        return result;
    }

    /**
     * @param sql 条件语句
     */
    @Override
    public void update(String sql) {
        try{
            database.execSQL(sql);
        }catch (Exception e){
            Log.e(TAG,"数据库语句异常，请检查！");
        }
    }

    /**
     * @param sql sql语句
     * @param args 条件语句
     */
    @Override
    public void update(String sql, String[] args) {
        try{
            database.execSQL(sql,args);
        }catch (Exception e){
            Log.e(TAG,"数据库语句异常，请检查！");
        }
    }

    /**
     * @param entity 删除的条件
     * @return
     */
    @Override
    public int delete(T entity) {

        HashMap<String, String> whereMap = getConditionValues(entity);

        Condition condition = new Condition(whereMap);

        int result = database.delete(tableName, condition.getWhereCause(), condition.getWhereCauseArry());
        return result;
    }

    /**
     * @param sql 条件语句
     */
    @Override
    public void delete(String sql) {
        try{
            database.execSQL(sql);
        }catch (Exception e){
            Log.e(TAG,"数据库语句异常，请检查！");
        }
    }

    /**
     * @param sql 条件语句
     * @param args 条件语句对应的值
     */
    @Override
    public void delete(String sql, String[] args) {
        try{
            database.execSQL(sql,args);
        }catch (Exception e){
            Log.e(TAG,"数据库语句异常，请检查！");
        }
    }

    /**
     * @param entity  查询的条件
     * @return
     */
    @Override
    public List<T> query(T entity) {
        return query(entity, null, null, null);
    }

    /**
     * @param entity  查询的条件
     * @param orderby 排序规则
     * @param startInt 开始位置
     * @param limit 限制数量
     * @return
     */
    @Override
    public List<T> query(T entity, String orderby, Integer startInt, Integer limit) {

        HashMap<String, String> whereMap = getConditionValues(entity);
        String lintStr = null;
        if (startInt != null && limit != null) {
            lintStr = startInt + " , " + limit;
        }

        Condition condition = new Condition(whereMap);

        Cursor cursor = database.query(tableName, null, condition.getWhereCause(), condition.getWhereCauseArry(), null, null, orderby, lintStr);

        List<T> result = getResult(cursor, entity);

        return result;
    }

    /**
     * @param cursor 查询语句获得游标
     * @param entity 结果的类型
     * @return
     */
    private List<T> getResult(Cursor cursor, T entity) {
        List result = new ArrayList();
        Object item;
        while (cursor.moveToNext()) {
            try {
                item = entity.getClass().newInstance();
                Iterator<Map.Entry<String, Field>> colNameIterator = cacheMap.entrySet().iterator();
                while (colNameIterator.hasNext()) {
                    Map.Entry<String, Field> entry = colNameIterator.next();
                    String colName = entry.getKey();
                    int colIndex = cursor.getColumnIndex(colName);
                    Field colField = entry.getValue();
                    Class type = colField.getType();
                    if (colIndex != -1) {
                        if (type == String.class) {
                            colField.set(item, cursor.getString(colIndex));
                        } else if (type == Double.class || type == double.class) {
                            colField.set(item, cursor.getDouble(colIndex));
                        } else if (type == Integer.class || type == int.class) {
                            colField.set(item, cursor.getInt(colIndex));
                        } else if (type == Long.class || type == long.class) {
                            colField.set(item, cursor.getLong(colIndex));
                        } else if (type == byte[].class) {
                            colField.set(item, cursor.getBlob(colIndex));
                        } else if (type == boolean.class || type == Boolean.class) {
                            String boolStr = cursor.getString(colIndex);
                            boolean b = false;
                            if ("true".equals(boolStr)) {
                                b = true;
                            }
                            colField.set(item, b);
                        } else if (type == short.class || type == Short.class) {
                            colField.set(item, cursor.getShort(colIndex));
                        } else {
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
    /**
     * @param cursor 查询语句获得游标
     * @param entity 结果的类型
     * @return
     */
    private List<T> getResult(Cursor cursor, Class<T> entity) {
        List result = new ArrayList();
        Object item;
        while (cursor.moveToNext()) {
            try {
                item = entity.newInstance();
                Iterator<Map.Entry<String, Field>> colNameIterator = cacheMap.entrySet().iterator();
                while (colNameIterator.hasNext()) {
                    Map.Entry<String, Field> entry = colNameIterator.next();
                    String colName = entry.getKey();
                    int colIndex = cursor.getColumnIndex(colName);
                    Field colField = entry.getValue();
                    Class type = colField.getType();
                    if (colIndex != -1) {
                        if (type == String.class) {
                            colField.set(item, cursor.getString(colIndex));
                        } else if (type == Double.class || type == double.class) {
                            colField.set(item, cursor.getDouble(colIndex));
                        } else if (type == Integer.class || type == int.class) {
                            colField.set(item, cursor.getInt(colIndex));
                        } else if (type == Long.class || type == long.class) {
                            colField.set(item, cursor.getLong(colIndex));
                        } else if (type == byte[].class) {
                            colField.set(item, cursor.getBlob(colIndex));
                        } else if (type == boolean.class || type == Boolean.class) {
                            String boolStr = cursor.getString(colIndex);
                            boolean b = false;
                            if ("true".equals(boolStr)) {
                                b = true;
                            }
                            colField.set(item, b);
                        } else if (type == short.class || type == Short.class) {
                            colField.set(item, cursor.getShort(colIndex));
                        } else {
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

    /**
     * @param map  表字段名和实际储存数据的缓存
     * @return
     */
    private ContentValues getContentValues(HashMap<String, String> map) {
        ContentValues contentValues = new ContentValues();
        Iterator<String> keyIterator = map.keySet().iterator();
        while (keyIterator.hasNext()) {
            String cacheKey = keyIterator.next();
            String cacheValue = map.get(cacheKey);
            if (cacheValue != null) {
                contentValues.put(cacheKey, cacheValue);
            }
        }
        return contentValues;
    }

    /**
     * @param entity 通过表映射缓存和传入需要存储的实际数据
     * @return
     */
    private HashMap<String, String> getValues(T entity) {
        HashMap<String, String> map = new HashMap<>();
        Iterator<Field> valueIterator = cacheMap.values().iterator();
        while (valueIterator.hasNext()) {
            String cacheKey;
            String cacheValue;
            Field colField = valueIterator.next();
            if (colField.getAnnotation(DbPrimaryField.class) != null) {
                continue;
            }
            if (colField.getAnnotation(DbField.class) != null) {
                cacheKey = colField.getAnnotation(DbField.class).value();
            } else {
                cacheKey = colField.getName();
            }
            try {
                if (null == colField.get(entity)) {
                    continue;
                }
                cacheValue = colField.get(entity).toString();
                map.put(cacheKey, cacheValue);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        return map;
    }

    /**
     * 这里有个漏洞，发现的朋友可以告诉我解决方案。
     * 联系方式：qq:794918578
     * @param entity 通过表映射缓存和传入需要存储的实际数据 这里去掉一些特殊数据类型的默认情况
     * @return
     */
    private HashMap<String, String> getConditionValues(T entity) {
        HashMap<String, String> map = new HashMap<>();
        Iterator<Field> valueIterator = cacheMap.values().iterator();
        while (valueIterator.hasNext()) {
            String cacheKey;
            String cacheValue;
            Field colField = valueIterator.next();
            if (colField.getAnnotation(DbPrimaryField.class) != null) {
                continue;
            }
            if (colField.getAnnotation(DbField.class) != null) {
                cacheKey = colField.getAnnotation(DbField.class).value();
            } else {
                cacheKey = colField.getName();
            }
            try {
                if (null == colField.get(entity)) {
                    continue;
                }
                cacheValue = colField.get(entity).toString();
                if ("0".equals(cacheValue) || "0.0".equals(cacheValue)) {
                    continue;
                }
                map.put(cacheKey, cacheValue);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        return map;
    }

    /**
     * 条件的整理
     */
    private class Condition {
        private String whereCause;
        private String[] whereCauseArry;

        public Condition(HashMap<String, String> whereCache) {
            Iterator<String> keyIterator = cacheMap.keySet().iterator();
            List<String> whereValues = new ArrayList<>();
            StringBuilder builder = new StringBuilder(" 1=1 ");
            while (keyIterator.hasNext()) {
                String key = keyIterator.next();

                String whereValue = whereCache.get(key);
                if (whereValue != null) {
                    builder.append("and " + key + " =? ");
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


    @Override
    public void executeSql(String sql) {
        try {
            database.execSQL(sql);
        }catch (Exception e){
            Log.e(TAG,"数据库语句异常，请检查！");
        }
    }

    @Override
    public void close() {
        if (database != null && database.isOpen()){
            database.close();
        }
    }
}
