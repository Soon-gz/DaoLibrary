package com.example.lemonlibrary.db;

import java.util.List;

/**
 * Created by ShuWen on 2017/1/9 0009.
 */

public interface IBaseDao<T> {
    /**
     * 插入数据
     * @param entity 插入的类型
     * @return 结果
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
     * @return 改变的数量
     */
    int  update(T entity, T where);
    /**
     *
     * @param sql 条件语句
     * @return 改变的数量
     */
    void  update(String sql);
    /**
     *
     * @param sql sql语句
     * @param args 条件语句
     * @return 改变的数量
     */
    void  update(String sql,String[] args);

    /**
     * 删除数据
     * @param where 条件语句
     * @return 删除数量
     */
    int  delete(T where);

    /**
     * 删除数据
     * @param sql 条件语句
     * @return 删除数量
     */
    void delete(String sql);

    /**
     * 删除数据
     * @param sql 条件语句
     * @param args 条件语句对应的值
     * @return 删除数量
     */
    void  delete(String sql,String[] args);

    /**
     * 查询数据
     */
    List<T> query(T where);

    /**
     *
     * @param where 查询条件的封装
     * @param orderBy 排序规则
     * @param startIndex 开始查询的位置
     * @param limit 限制数量
     * @return 返回结果的集合
     */
    List<T> query(T where, String orderBy, Integer startIndex, Integer limit);

    /**
     * @param sql 执行的sql语句
     * @return 返回的结果集合
     */
    List<T> query(String sql,Class<T> entity);

    /**
     * @param sql 执行语句
     * @param args 对应的sql中的条件
     * @return 返回结果集合
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
