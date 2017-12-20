package xd.medicine.controller;

import com.github.pagehelper.PageInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import xd.medicine.entity.bo.Doctor;
import xd.medicine.service.DoctorService;

import java.util.Date;
import java.util.List;

/**
 * created by xdCao on 2017/12/19
 */
@RequestMapping("/doctor")
@RestController
public class DoctorController {

    private static final Logger LOGGER= LoggerFactory.getLogger(DoctorController.class);

    @Autowired
    private DoctorService doctorService;

    @RequestMapping(value = "/single",method = RequestMethod.GET)
    public FrontResult getDoctorById(@RequestParam int doctorId){
        Doctor doctor = doctorService.getDoctorById(doctorId);
        if (doctor!=null){
            return new FrontResult(200,doctor,null);
        }else {
            return new FrontResult(500,null,"没有该医生");
        }
    }

    @RequestMapping(value = "/all",method = RequestMethod.GET)
    public FrontResult getAllDoctors(){
        List<Doctor> allDoctors = doctorService.getAllDoctors();
        if (allDoctors!=null&&allDoctors.size()>0){
            return new FrontResult(200,allDoctors,null);
        }else {
            return new FrontResult(500,null,"医生列表为空");
        }
    }

    @RequestMapping(value = "/page",method = RequestMethod.GET)
    public FrontResult getDoctorByPage(@RequestParam Integer page,@RequestParam Integer rows){
        PageInfo<Doctor> doctorsByPage = doctorService.getDoctorsByPage(page, rows);
        return new FrontResult(200,doctorsByPage,null);
    }

    @RequestMapping(value = "/add",method = RequestMethod.POST)
    public FrontResult addNewDoctor(@RequestParam String name,
                                @RequestParam String account,
                                @RequestParam String password,
                                @RequestParam byte position,
                                @RequestParam String officeLocation,
                                @RequestParam String officePhone,
                                @RequestParam byte department,
                                @RequestParam byte title,
                                @RequestParam byte workage,
                                @RequestParam byte degree,
                                @RequestParam boolean isFree){

        Integer countDoctorsByAccount = doctorService.countDoctorsByAccount(account);
        if (countDoctorsByAccount>0){
            return new FrontResult(500,null,"该账户名已存在");
        }

        Doctor doctor=new Doctor();
        doctor.setName(name);
        doctor.setAccount(account);
        doctor.setPassword(password);
        doctor.setPosition(position);
        doctor.setOfficeLocation(officeLocation);
        doctor.setOfficePhone(officePhone);
        doctor.setDepartment(department);
        doctor.setDegree(degree);
        doctor.setTitle(title);
        doctor.setWorkage(workage);
        doctor.setIsFree(isFree);
        doctor.setRegistryDate(new Date());
        doctorService.insertDoctor(doctor);
        return new FrontResult(200,doctor,null);
    }

    //todo update doctor

}
