package totoro.top.java2html;

import java.util.ArrayList;
import java.util.List;

/**
 * 一些常量
 * 
 * @author 黄龙三水
 *
 * @since 1.0
 */
public class Constants {
	// pre ：代码块
	public static final String START_PRE = "<pre>";
	public static final String END_PRE = "</pre>";
	// 关键字样式
	public static final String START_NOBR_KEY_WORD = "<nobr id=\"k\">";
	// 全局变量样式
	public static final String START_NOBR_GLOBAL = "<nobr id=\"g\">";
	// 局部变量样式
	public static final String START_NOBR_FIELD = "<nobr id=\"f\">";
	// 绿色注释样式
	public static final String START_NOBR_NOTE_GREEN = " <nobr id=\"n-g\">";
	// 蓝色注释样式
	public static final String START_NOBR_NOTE_BLUE = " <nobr id=\"n-b\">";
	// 注解样式
	public static final String START_NOBR_ANNOTATION = " <nobr id=\"a\">";
	// 字符串样式
	public static final String START_NOBR_STRING = "<nobr id=\"s\">";
	// 制表符
	public static final String TAB_CHARACTER = "&#9;";
	// 换行符
	public static final String BR = "<br>";
	public static final String END_NOBR = "</nobr>";
	
	public static final boolean GLOBAL_HEIGH_LIGHT = true;
	public static final boolean FIELD_HEIGH_LIGHT = true;
	
	public static final List<String> globals = new ArrayList<String>();
	public static final List<String> fields = new ArrayList<String>();
}
