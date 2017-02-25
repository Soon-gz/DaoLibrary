package com.example.administrator.lemondao;

import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.example.administrator.lemondao.dataModel.FileDao;
import com.example.administrator.lemondao.dataModel.FileModel;
import com.example.administrator.lemondao.dataModel.User;
import com.example.administrator.lemondao.dataModel.UserDao;
import com.example.lemonlibrary.db.BaseDaoFactory;
import com.example.lemonlibrary.db.IBaseDao;

import java.io.File;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private IBaseDao<User> iBaseDao;
    String dbFile;
    int i = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //数据库地址  文件夹
        dbFile = Environment.getExternalStorageDirectory().getAbsolutePath() + "/myDb";
        //建立数据库和表的链接
        iBaseDao = BaseDaoFactory.getInstance().getSqliteComponent(UserDao.class,User.class,dbFile,"myselft.db");
    }

    public void click(View view){
        switch (view.getId()){
            //添加用户
            case R.id.add_user:
                short si = 123;
                User user = new User("鬼刀","六极之地","123456",0,"user_"+i,true,1.234,457864745l,si);
                iBaseDao.insert(user);
                i++;
                break;
            //删除用户
            case R.id.delete_user:
                User user2 = new User();
                user2.setUser_id("user_0");
                //由于手机数据库无法存放布尔值，在寻找条件时，必须对布尔值进行赋值。否则默认是false！
                user2.setMe(true);
                iBaseDao.delete(user2);
                break;
            //查询用户
            case R.id.query_user:
                User user1 = new User();
                user1.setName("鬼刀");
                //由于手机数据库无法存放布尔值，在寻找条件时，必须对布尔值进行赋值。否则默认是false！
                user1.setMe(true);
                List<User> users = iBaseDao.query(user1);
                for (User user3:users) {
                    Log.i("tag00",user3.toString());
                    Log.i("tag00","================================");
                }
                break;
            //更新用户
            case R.id.update_user:
                User newUser = new User();
                newUser.setName("天南之剑");

                User where = new User();
                where.setMe(true);
                where.setUser_id("user_1");

                iBaseDao.update(newUser,where);
                break;
        }
    }
}
