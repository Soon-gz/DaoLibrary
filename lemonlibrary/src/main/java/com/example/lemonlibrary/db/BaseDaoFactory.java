package com.example.lemonlibrary.db;

import android.database.sqlite.SQLiteDatabase;

import java.io.File;

/**
 * Created by ShuWen on 2017/1/9 0009.
 */

public class BaseDaoFactory {
    private String sqliteDatabasePath;

    private SQLiteDatabase sqLiteDatabase;

    private static  BaseDaoFactory instance=new BaseDaoFactory();

    private BaseDaoFactory()
    {
    }

    public  synchronized  <T extends  BaseDao<M>,M> T
    getSqliteComponent(Class<T> clazz,Class<M> entityClass,String sqliteDbPath,String dbName)
    {

        File file=new File(sqliteDbPath);
        if(!file.exists())
        {
            file.mkdirs();
        }
        sqliteDatabasePath= file.getAbsolutePath() + File.separator + dbName;

        if(sqLiteDatabase != null && sqLiteDatabase.isOpen())
        {
            sqLiteDatabase.close();
        }

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



    public  static  BaseDaoFactory getInstance()
    {
        return instance;
    }

}
