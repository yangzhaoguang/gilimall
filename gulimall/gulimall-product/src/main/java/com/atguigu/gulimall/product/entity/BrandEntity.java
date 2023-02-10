package com.atguigu.gulimall.product.entity;

import com.atguigu.common.valid.AddGroup;
import com.atguigu.common.valid.ListValue;
import com.atguigu.common.valid.UpdateGroup;
import com.atguigu.common.valid.UpdateStatus;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.io.Serializable;

import lombok.Data;
import org.hibernate.validator.constraints.URL;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.*;

/**
 * 品牌
 * 
 * @author YZG
 * @email yangzhaoguang09@gmail.com
 * @date 2022-12-14 16:33:59
 */
@Data
@TableName("pms_brand")
public class BrandEntity implements Serializable {
	private static final long serialVersionUID = 1L;

	/**
	 * 品牌id
	 */
	@TableId
	@Null(message = "新增时不能指定ID",groups = {AddGroup.class})
	@NotNull(message = "修改时必须指定ID",groups = {UpdateGroup.class})
	private Long brandId;
	/**
	 * 品牌名
	 */
	@NotBlank(message = "品牌名成不能为空",groups = {AddGroup.class,UpdateGroup.class})
	@Null(groups = {UpdateStatus.class})
	private String name;
	/**
	 * 品牌logo地址
	 */
	@NotBlank(message = "logo地址不能为空",groups = {AddGroup.class})
	@URL(message = "url地址不合法",groups = {AddGroup.class,UpdateGroup.class})
	private String logo;
	/**
	 * 介绍
	 */
	private String descript;
	/**
	 * 显示状态[0-不显示；1-显示]
	 */
	@ListValue(vals ={0,1},groups = {UpdateStatus.class})
	private Integer showStatus;
	/**
	 * 检索首字母
	 *  使用正则校验
	 */
	@Pattern(regexp = "^[a-zA-Z]$",message = "检索首字母必须在a~z或A~Z范围内，并且只有一位",groups = {AddGroup.class,UpdateGroup.class})
	@NotBlank(message = "检索首字母不能为空",groups = {AddGroup.class})
	private String firstLetter;
	/**
	 * 排序
	 */
	@Min(value = 0,message = "排序必须是一个大于0的整数",groups = {AddGroup.class,UpdateGroup.class})
	private Integer sort;

}
