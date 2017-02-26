package com.example.lemonlibrary.db;

import java.util.List;

/**
 * Created by ShuWen on 2017/1/9 0009.
 */

public interface IBaseDao<T> {
    /**
     * 插入数据
     * @param entity 插入的类型
     * @return
     */
    Long insert(T entity);

    /**
     *
     * @param sql 插入的sql语句
     */
    void insert(String sql);

    /**
     *
     * @param entity 更新的数据
     * @param where 条件语句
     * @return
     */
    int  update(T entity, T where);
    /**
     *
     * @param sql 条件语句
     */
    void  update(String sql);
    /**
     *
     * @param sql sql语句
     * @param args 条件语句
     */
    void  update(String sql,String[] args);

    /**
     * 删除数据
     * @param where 条件语句
     */
    int  delete(T where);

    /**
     * 删除数据
     * @param sql 条件语句
     */
    void delete(String sql);

    /**
     * 删除数据
     * @param sql 条件语句
     * @param args 条件语句对应的值
     */
    void  delete(String sql,String[] args);

    /**
     * @param where
     * @return
     */
    List<T> query(T where);

    /**
     *
     * @param where 查询条件的封装
     * @param orderBy 排序规则
     * @param startIndex 开始查询的位置
     * @param limit 限制数量
     * @return
     */
    List<T> query(T where, String orderBy, Integer startIndex, Integer limit);

    /**
     * @param sql
     * @param entity
     * @return
     */
    List<T> query(String sql,Class<T> entity);

    /**
     * @param sql
     * @param entity
     * @param args
     * @return
     */
    List<T> query(String sql,Class<T> entity,String[] args);

    /**
     * @param sql 执行sql语句，可以删除表或者删除数据库，设置主键等一系列操作
     */
    void executeSql(String sql);

    /**
     * 关闭数据库
     */
    void close();
}
