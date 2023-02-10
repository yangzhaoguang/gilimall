package com.atguigu.common.valid;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.HashSet;
import java.util.Set;

/**
 *
 * Author: YZG
 * Date: 2022/12/29 22:03
 * Description:
 *  自定义注解解析器
 */
// 泛型中的俩个属性：第一个是自定义的注解，第二个是校验的参数类型
public class ListValueConstraintValidator implements ConstraintValidator<ListValue, Integer> {
    private Set<Integer> set = new HashSet<>();

    /*
     * constraintAnnotation 可以获取使用注解时的指定值
     *  {0,1}
     * */
    @Override
    public void initialize(ListValue constraintAnnotation) {
        int[] vals = constraintAnnotation.vals();
        for (int val : vals) {
            set.add(val);
        }
    }

    /**
     * @description 判断校验是否通过
     * @date 2022/12/29 22:06
     * @param value 需要校验的值
     * @param context
     * @return boolean
     */
    @Override
    public boolean isValid(Integer value, ConstraintValidatorContext context) {
        // 如果指定的值中包含需要校验的值就返回 true
        return set.contains(value);
    }
}
