package com.example.lemonlibrary.db;

import android.database.sqlite.SQLiteDatabase;

import java.io.File;

/**
 * Created by ShuWen on 2017/1/9 0009.
 */

public class BaseDaoFactory {
    private String sqliteDatabasePath;

    private SQLiteDatabase sqLiteDatabase;

    private static  BaseDaoFactory instance;

    private static final byte[] lock = new byte[0];

    private BaseDaoFactory()
    {
    }

    /**
     * 建立数据库与表的链接
     */
    public  synchronized  <T extends  BaseDao<M>,M> T
    getSqliteComponent(Class<T> clazz,Class<M> entityClass,String sqliteDbPath,String dbName)
    {

        File file=new File(sqliteDbPath);
        if(!file.exists())
        {
            file.mkdirs();
        }
        String newSqlitePath = file.getAbsolutePath() + File.separator + dbName;
        //不同数据库切换，需要先关闭上个数据库
        if (sqliteDatabasePath != null && !sqliteDatabasePath.equals(newSqlitePath)){
            if(sqLiteDatabase != null && sqLiteDatabase.isOpen())
            {
                sqLiteDatabase.close();
            }
        }
        sqliteDatabasePath = newSqlitePath;
        openDatabase();
        BaseDao baseDao=null;
        try {
            baseDao=clazz.newInstance();
            baseDao.init(entityClass,sqLiteDatabase);
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return (T) baseDao;
    }


    private void openDatabase() {
        this.sqLiteDatabase=SQLiteDatabase.openOrCreateDatabase(sqliteDatabasePath,null);
    }

    /**
     * 采用DLC单利，线程安全
     * @return
     */
    public  static  BaseDaoFactory getInstance()
    {
        if (instance == null){
            synchronized (lock){
                if (instance == null){
                    instance = new BaseDaoFactory();
                }
            }
        }
        return instance;
    }

}
