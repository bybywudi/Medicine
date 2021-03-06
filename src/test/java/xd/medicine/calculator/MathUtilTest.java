package xd.medicine.calculator;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import static xd.medicine.utils.MathUtils.*;

/**
 * created by liubotao
 */

/* MathUtil测试 */
public class MathUtilTest {

    /*@Test
    public void test1() {
        float a = (float) 1 / 12;
        System.out.println(a);
        for (int i = 0; i < 10; i++)
            System.out.println((0.3 - a) + new Random().nextFloat() * (2 * a));
    }*/

    @Test
    public void test2(){
        for(int i = 0; i<10; i++) {
            List<Integer> list = sampling(10000, 5);
            for (int num : list) {
                System.out.print(num + " ");
            }
            System.out.println();
        }
    }

    @Test
    public void test3(){
        List<Integer> list = new ArrayList<>();
        list.add(1);
        list.add(1);
        list.add(3);
        list.add(3);
        list.add(2);
        list.add(2);
        System.out.println(SensitivityCalculator.calSensitivity(list));
    }

    @Test
    public void test4(){
        for(int i =0;i<30;i++){
        //    System.out.println(getRandomFloat(0,2));
        }

        int cc[] = getRandomArray(1,10,5);
        for(int j=0;j<cc.length;j++){
            System.out.println(cc[j]);
        }

    }

    @Test
    public void test5(){
        //for(int i =0 ; i < 10;i++)
            System.out.println(new Random().nextFloat());
    }




}
