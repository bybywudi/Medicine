package xd.medicine.calculator;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import xd.medicine.entity.bo.Doctor;
import xd.medicine.entity.bo.PostDuty;
import xd.medicine.entity.bo.PostDutyLog;
import xd.medicine.entity.bo.ProDuty;
import xd.medicine.service.DoctorService;
import xd.medicine.service.PostDutyLogService;
import xd.medicine.service.PostDutyService;
import xd.medicine.service.ProDutyService;

import java.util.Date;
import java.util.List;

import static xd.medicine.utils.MathUtils.getRandomFloat;

/**
 * created by liubotao
 */

@RunWith(SpringRunner.class)
@SpringBootTest
/* 只是用来自动生成insert语句，没有其他作用 */
public class DbInsertHelperTest {
    @Autowired
    private DoctorService doctorService;
    @Autowired
    private PostDutyService postDutyService;
    @Autowired
    private PostDutyLogService postDutyLogService;

    @Test
    public void prtSql(){
        //INSERT INTO `user_log` VALUES ('17', '10', 'Bob', '60', 'bbb', '2017-12-25', '1', 'X');
        //INSERT INTO `sys_log` VALUES ('23', '10', 'Bob', '63', 'a', '8', '2017-12-13');
        int a=22,b=27;
        for(int i = 1 ; i< 13 ; i++){
            for(int j = 0 ; j< 20 ; j++) {
                a++;
                System.out.println("INSERT INTO `user_log` VALUES ('"+a+"', '"+ i +"', '"+doctorService.getDoctorById(i).getName()+"', '"+ (int)getRandomFloat(0 ,100)+"', 'bbb', '2017-12-25', '1', 'X');");
            }
        }

        for(int i = 1 ; i< 13 ; i++){
            for(int j = 0 ; j< 20 ; j++) {
                b++;
                System.out.println("INSERT INTO `sys_log` VALUES ('"+b+"', '"+i+"', '"+doctorService.getDoctorById(i).getName()+"', '"+ (int)getRandomFloat(0 ,100)+"', 'a', '8', '2017-12-13');");
            }
        }
    }




}
