package cn.habitdiary.form.service.impl;

import cn.habitdiary.form.dao.FormDao;
import cn.habitdiary.form.entity.Form;
import cn.habitdiary.form.entity.FormDefinition;
import cn.habitdiary.form.entity.FormItem;
import cn.habitdiary.form.entity.User;
import cn.habitdiary.form.service.FormService;
import cn.habitdiary.form.utils.ExcelUtil;
import cn.habitdiary.form.utils.ObjectIOUtil;
import cn.habitdiary.form.utils.UUIDUtil;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
public class FormServiceImpl implements FormService {
    @Autowired
    private FormDao formDao;
    @Value("${root-location}")
    private String rootLocation;


    @Override
    public String addForm(String formname, String formdesc, String begintime, String endtime,
                        String password,User loginUser,String[] name,String[] info) {
            Integer userid = loginUser.getUserid();
            Integer formstatus = 0;
            File rootdir = new File(rootLocation);
            if (!rootdir.exists()) {
                rootdir.mkdir();
            }
            File userdir = new File(rootLocation + "/" + userid);
            if (!userdir.exists()) {
                userdir.mkdir();
            }
            String uuid = UUIDUtil.getUUID();
            String formpath = rootLocation + "/" + userid + "/" + formname +
                    "(" + uuid + ")" + ".xls";

        password = DigestUtils.sha1Hex(password);
        formDao.addForm(uuid,formname,formpath,formstatus,formdesc,begintime,endtime,userid,password);
        //保存FormDefinition文件，保存Excel文件
        ExcelUtil.createExcel(formname,formpath,name);
        List<FormItem> list = new ArrayList<>();
        int len = name.length;
        for(int i = 0;i < len;i++) {
            list.add(new FormItem(name[i],info[i]));
        }
        FormDefinition formDefinition = new FormDefinition(list);
        String objectPath = rootLocation + "/" + userid + "/" + formname +
                "(" + uuid + ")" + ".txt";
        try {
            ObjectIOUtil.createObj(objectPath,formDefinition);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return uuid;

    }

    @Override
    public Form selectForm(Integer formid,String uuid, String formname, Integer userid) {
        return formDao.selectForm(formid,uuid,formname,userid);
    }

    @Override
    public FormDefinition getFormDefinition(Integer userid, String uuid) {
          String formname = selectForm(null,uuid,null,userid).getFormname();
          String objectPath = rootLocation + "/" + userid + "/" + formname +
                "(" + uuid + ")" + ".txt";
        try {
            return ObjectIOUtil.readObj(objectPath);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }



    @Override
    public boolean checkPassword(String password, Integer formid) {
            String pwd = DigestUtils.sha1Hex(password);
            Form form = formDao.selectForm(formid,null,null,null);
            if(pwd.equals(form.getPassword()))
                return true;
            return false;
    }
}
