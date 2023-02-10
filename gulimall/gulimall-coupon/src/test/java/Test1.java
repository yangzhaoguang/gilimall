import org.junit.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

/**
 *
 * Author: YZG
 * Date: 2023/2/7 18:08
 * Description: 
 */
public class Test1 {

    @Test
    public void test(){
        // 获取当前日期 2023-02-06
        LocalDate localDate = LocalDate.now();
        // 获取时分秒: 00:00:00
        LocalTime now = LocalTime.MIN;
        // 2023-02-06 00:00:00
        LocalDateTime dateTime = LocalDateTime.of(localDate, now);
        String format = dateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        System.out.println(format);

        // 获取当前日期的后三天 2023-02-08
        LocalDate localDate1 = LocalDate.now().plusDays(2);
        // 获取时分秒: 11:59:59
        LocalTime now1 = LocalTime.MAX;
        // 2023-02-08 23:59:59
        LocalDateTime dateTime1 = LocalDateTime.of(localDate1, now1);
        // 指定时间格式格式化
        String format1 = dateTime1.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        System.out.println(format1);
    }
}
