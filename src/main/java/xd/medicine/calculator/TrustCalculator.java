package xd.medicine.calculator;
import com.github.pagehelper.PageInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import xd.medicine.entity.bo.*;
import xd.medicine.entity.dto.AvaDoctor;
import xd.medicine.entity.dto.DoctorTrustResult;
import xd.medicine.entity.dto.PatientWithTrust;
import xd.medicine.service.CommentService;
import xd.medicine.service.DoctorService;
import xd.medicine.service.PatientService;
import xd.medicine.service.ServiceImpl.DoctorServiceImpl;

import javax.print.Doc;
import java.util.*;

import static xd.medicine.calculator.Constants.*;
import static xd.medicine.utils.MathUtils.*;

/**
 * created by liubotao
 */

@Component
public class TrustCalculator {

    @Autowired
    private PatientService patientService;

    @Autowired
    private CommentService commentService;

    @Autowired
    private DoctorService doctorService;

    /*
     * 输入病人id，获得当前属于available状态的可信主体集
     * mode: 0--紧急情况，1-未知情况
     */
    public List<DoctorTrustResult> getAvaTs( int patientId ){
        List<DoctorTrustResult> doctorTrustResultList = getTs(patientId);
        for(int i = 0; i< doctorTrustResultList.size(); i++){
            if(!doctorTrustResultList.get(i).getAva()){
                doctorTrustResultList.remove(i);
                i--;
            }
        }
        return doctorTrustResultList;
    }


    /*
    *计算可信度
    *输入一个病人id，返回匹配到的主体集及其匹配可信度、历史行为可信度、总体可信度
     */
    public List<DoctorTrustResult> getTs( int patientId ) {

        PatientWithTrust patientWithTrust = patientService.getPatientById(patientId);

        List<Doctor> doctorList = patientService.getSisDoctorsByPatientId(patientId);  //获得满足科室要求的所有医生，即候选主体集合SIS
        List<DoctorTrustResult> doctorTrustResultList = new ArrayList<>();
        for (Doctor doctor : doctorList) {
            /*if((!(doctor.getIsin() & doctor.getIsFree()))& avaOnly){
                continue;
            }*/

            //System.out.println(doctorTrustResult.toString());
            DoctorTrustResult doctorTrustResult = calDocBsTrust(patientWithTrust , doctor);
            if(doctorTrustResult!=null) {
                doctorTrustResultList.add(doctorTrustResult);
            }
        }

        Collections.sort(doctorTrustResultList, new Comparator<DoctorTrustResult>() {
            @Override
            public int compare(DoctorTrustResult o1, DoctorTrustResult o2) {
                return new Float(o2.getTrust()).compareTo(new Float(o1.getTrust()));
            }
        });
        return doctorTrustResultList;
    }

    /*
    * 计算单个医生的基本可信度
     */
    public DoctorTrustResult calDocBsTrust(PatientWithTrust patientWithTrust, Doctor doctor){
        float mt,hbt,rcm,rep,trust;
        if (patientWithTrust.getPatient().getDoctorId() == doctor.getId()) {
                /* 如果是主治医生，所有可信度直接为1 */
            mt = 1;
            hbt = 1;
            trust = 1;
            rcm = 1;
            rep = 1;
        } else {
            mt = calDocMt(doctor , patientWithTrust);
            rcm = calDocRCM(doctor);
            rep = calDocREP(doctor);
            if(rcm<0||rcm>1||rep<0||rep>1){
                System.out.println("ERROR RCM OR REP!医生id："+ doctor.getId()+"医生姓名："+doctor.getName());
                return null;
            }
            hbt = rcm<rep?rcm:rep;     //HBT = min(RCM,REP)
            trust = mt*TRUST_U1 + hbt*(1-TRUST_U1);
        }

        boolean ava = doctor.getIsin()&doctor.getIsFree();
        int grade;
        if(trust==1){
            grade = 1;
        }else if(trust>=Constants.getBeta1()){
            grade = 2;
        }else if(trust>=Constants.getBeta2()){
            grade = 3;
        }else if(trust>0){
            grade = 4;
        }else{
            grade = 5;
        }

        DoctorTrustResult doctorTrustResult = new DoctorTrustResult(doctor.getId(),doctor.getName(), mt, rcm, rep, hbt, trust, doctor.getIsin() , doctor.getIsFree(), ava,grade);
        return doctorTrustResult;
    }



    /*
    *给定一个医生和一个病人，计算MT
     */
    public float calDocMt(Doctor doctor, PatientWithTrust patientWithTrust){
        float mt = 0;
        float[] wi = new float[4];
        /* 计算随机波动的权值wi */
        for (int i = 0; i < 3; i++) {
            wi[i] = getRandomFloat((float) 0.25 - MT_ALPHA, (float) 0.25 + MT_ALPHA);
        }
        wi[3] = 1 - wi[0] - wi[1] - wi[2];

        List<Integer> pTr = new ArrayList<>();
        pTr.add(patientWithTrust.getTrustAttr().getDemandTitle()& 0xFF);
        pTr.add(patientWithTrust.getTrustAttr().getDemandWorkage()& 0xFF);
        pTr.add(patientWithTrust.getTrustAttr().getDemandDegree()& 0xFF);

        List<Integer> dTr = new ArrayList<>();
        dTr.add(doctor.getTitle() & 0xFF);
        dTr.add(doctor.getWorkage() & 0xFF);
        dTr.add(doctor.getDegree() & 0xFF);

        List<Float> results = calDocMtHelper( pTr, dTr );

        /* 根据职称、工龄、学位计算MT */
        mt += 1 * wi[0]; //科室已经匹配，科室的匹配值恒为1
        mt += results.get(0) * wi[1];//计算职称的匹配值
        mt += results.get(1) * wi[2];//计算工作年龄的匹配值
        mt += results.get(2) * wi[3];//计算学历信息的匹配值
        return mt;
    }

    /*
    *计算MT的辅助方法，List中按照职称-工龄-学历的顺序存放
     */
    public List<Float> calDocMtHelper(List<Integer> pTr, List<Integer> dTr ) {
        List<Float> results= new ArrayList<>();

        int n = 5;
        for (int i = 0; i < 3 ; i++) {
            if( i == 2 ) n = 3; //职称和工龄有五种，如果是学历计算，只有三种
            int p = pTr.get(i);
            int d = dTr.get(i);
            float f;
            if (p <= d) {
                //System.out.print(THSVALUE + (1-TRUSTU)/ (3 - p)*( d - p )+"--");
                //System.out.println(THSVALUE + (1-TRUSTU)/ (3 - p)*( d - p +1));
                f = getRandomFloat(THS_VALUE + (1 - THS_VALUE) / (n - p) * (d - p), THS_VALUE + (1 - THS_VALUE) / (n - p) * (d - p + 1));
            } else {
                //System.out.print(THSVALUE / p * d + "---");
                //System.out.println(THSVALUE / p * (d+1));
                f = getRandomFloat(THS_VALUE / p * d , THS_VALUE / p * (d + 1));
            }
            results.add(f);
        }
        return results;
    }

    /*
    *给定一个医生，通过用户日志计算其RCM
    */
    public float calDocRCM(Doctor doctor){
        List<UserLog> userLogList = commentService.getAllUserLogsByDoctor(doctor.getId());
        if(userLogList.size()< HBT_N){
            System.out.println("用户日志样本数量不足！！!医生id："+ doctor.getId()+"，医生姓名："+doctor.getName());
            return (float)0.5;   //样本不足时，取默认值0.5
        }

        List<Float> samplingAveValue = new ArrayList<>(); //用来存放每次抽样后计算出来的平均值
        for(int i=0; i<HBT_M; i++){
            int sum = 0;
            List<Integer> list = sampling(userLogList.size(),HBT_N); //进行抽样
            List<Integer> valueList = new ArrayList<>(); //存放抽样得到的值
            for(int j=0 ; j < HBT_N; j++){
                int f=(userLogList.get(list.get(j)-1).getEvaluateValue())&0xFF;
                valueList.add(f);
                sum+=f;
            }
            float ave = (float)sum/HBT_N/100; //评价值为0-100，计算时以1为最大值进行量化
            /* 判断本次抽样中是否有超过阈值的不可信评价 */
            boolean flag = true;
            for(int value:valueList){
                if(Math.abs((float) value/100 - ave) > DIFFER_MAX) {
                   // System.out.println("不可信的一次抽样！value="+value+",ave="+ave);
                    flag = false;
                    break;
                }
            }
            if(flag) {
                samplingAveValue.add(ave);
            }
        }

        if(samplingAveValue.size()<1){
            System.out.println("ERROR！！多次抽样样本均不可信！！!医生id："+ doctor.getId()+"，医生姓名："+doctor.getName());
            return (float)0.5;   //样本不足时，取默认值0.5
        }

        /* 已经获得所有抽样值，下面计算HBT */
        float deno = 0; float rcm = 0;

        /* 先计算公式中的分母 */
        /*for(float i : samplingAveValue){
            deno += 1-i;
        }
        for(float i : samplingAveValue){
            rcm += i* (1-i) / deno;
        }*/

        /* 改动之后，权值固定 */
        float w = (float) 1/samplingAveValue.size();
        for(float i : samplingAveValue){
            rcm += i * w;
        }

        return rcm;
    }


    /*
    *给定一个医生，通过系统日志计算其REP
    */
    public float calDocREP(Doctor doctor){
        List<SysLog> sysLogList = commentService.getAllSysLogsByDoctor(doctor.getId());
        if(sysLogList.size()==0){
            System.out.println("系统日志样本数量不足！！!医生id："+ doctor.getId()+"医生姓名："+doctor.getName());
            return (float)0.5;   //样本不足时，取默认值0.5
        }
        List<Integer> list = new ArrayList<>();
        for(SysLog sysLog : sysLogList){
            list.add(sysLog.getEvaluateValue()&0xFF);
        }
        return getMiddle(list)/100;
    }

    /*
    *给定一个医生，计算其HBT
     */
    /*public float calDocHbt(Doctor doctor){
        float rcm = calDocRCM(doctor);
        float rep = calDocREP(doctor);
        if(rcm<0||rcm>1||rep<0||rep>1){
            System.out.println("ERROR RCM OR REP");
            return -1;
        }
        return rcm<rep?rcm:rep;     //HBT = min(RCM,REP)
    }*/




}
