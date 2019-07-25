package totoro.top.java2html;

public class Constants {
	// 带特定CSS样式的HTML标签，tips：<nobr></nobr>为强制不换行标签
	
	/* Java代码块 */
	public static final String START_PRE = "<pre>";
	public static final String END_PRE = "</pre>";
	
	/* Java关键字紫色加粗 */
	public static final String START_NOBR_KEY_WORD= " <nobr class=\"key\">";
	/* Java绿色字体注释 */
	public static final String START_NOBR_NOTE_GREEN= " <nobr class=\"note-green\">";
	/* Java蓝色字体注释 */
	public static final String START_NOBR_NOTE_BLUE= " <nobr class=\"note-blue\">";
	/* Java灰色注解 */
	public static final String START_NOBR_ANNOTATION= " <nobr class=\"anno\">";
	/* Java蓝色字符串 */
	public static final String START_NOBR_STRING= " <nobr class=\"string\">";
	
	public static final String END_NOBR = "</nobr>";
	public static final String BR = "<br>";
	
}
