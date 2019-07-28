package totoro.top.java2html;

import java.util.ArrayList;
import java.util.List;

/**
 * 代码转换逻辑
 * 
 * @author 黄龙三水
 *
 * @since 1.2
 */
public final class Composition {
	// 需要转换的Java代码
	private String javaCode = "";
	// 转换后的HTML模板代码
	private StringBuilder htmlCode = new StringBuilder();

	// Java代码中的全局变量
	private List<String> globals = new ArrayList<String>();
	// Java代码中的局部变量
	private List<String> fields = new ArrayList<String>();

	public StringBuilder getHtmlCode() {
		return htmlCode;
	}

	public Composition(String java) {
		if (java != null) {
			javaCode = java;
		}
	}

	/*
	 * 标志注释是否在当前行结束 如果为真时 ：说明下一行任然是注释的内容
	 */
	private boolean greenStatus, blueStatus;
	/*
	 * defineFieldsIndex ：标志当前定义新方法或构造器，如果定义新方法或构造器,该值要自增。 oldFieldsIndex
	 * ：小于defineFieldsIndex时，说明定义了新方法，上个方法或构造器的局部变量清空。
	 */
	private int defineFieldsIndex = 0, oldFieldsIndex = 0;
	// 字符串结束标志
	private String[] ends = { "", ",", ";", ")", "))", ")))", "))))", "),", ")),", "))),", ")))),", ");", "));", ")));",
			"))));", ":", "?" };
	private int stringEndIndex = -1;

	public String toHtml() {
		htmlCode.append(Constants.START_PRE);
		String[] lines = javaCode.split("\n");
		for (int i = 0; i < lines.length; i++) {
			String[] lineSplitByT = lines[i].split("\t");
			for (String line : lineSplitByT) {
				if (line.length() == 0) {
					/*
					 * 插入适合显示的制表符，不再是原始的Tab键宽度： 因为HTML代码中不会识别Tab制表符，所以用空格来代替
					 */
					htmlCode.append(Constants.TAB_CHARACTER);
				} else {
					// 定义了新方法或构造器，清空局部变量
					if (oldFieldsIndex < defineFieldsIndex) {
						fields.clear();
						oldFieldsIndex = defineFieldsIndex;
					}
					// 注释内容高亮
					if (isNote(line)) {
						continue;
					}
					// 注解高亮
					if (isAnno(line)) {
						continue;
					}
					// 以注释结尾的代码行
					if (isEndWithNote(line)) {
						continue;
					}
					// 定义全局变量的高亮
					if (isDefineGlobal(line)) {
						continue;
					}
					// 定义局部变量的高亮
					if (isDefineField(line)) {
						continue;
					}
					// 定义方法或构造器的高亮
					if (isDefineMethodOrConstructor(line)) {
						defineFieldsIndex++;
						continue;
					}
					// 其它部分高亮
					String[] words = line.split(" ");
					for (int j = 0; j < words.length; j++) {
						String word = words[j];
						if (isString(words, j)) {
							j = stringEndIndex;
							continue;
						}
						if (isKeyAsWord(word)) {
							continue;
						}
						if (isAnno(word)) {
							continue;
						}
						htmlCode.append(usedVariable(word) + " ");
					}

				}
			}
			lineSplitByT = null;
			htmlCode.append(Constants.BR);
		}
		lines = null;
		htmlCode.append(Constants.END_PRE);
		return htmlCode.toString();
	}

	private boolean isEndWithNote(String line) {
		if (line.contains("//")) {
			String note = line.substring(line.lastIndexOf("//"));
			line = line.substring(0, line.lastIndexOf("//"));
			// 定义全局变量的高亮
			if (isDefineGlobal(line)) {
				isNote(note);
				return true;
			}
			// 定义局部变量的高亮
			if (isDefineField(line)) {
				isNote(note);
				return true;
			}
			// 定义方法或构造器的高亮
			if (isDefineMethodOrConstructor(line)) {
				defineFieldsIndex++;
				isNote(note);
				return true;
			}
			// 其它部分高亮
			String[] words = line.split(" ");
			for (int j = 0; j < words.length; j++) {
				String word = words[j];
				if (isString(words, j)) {
					j = stringEndIndex;
					continue;
				}
				if (isKeyAsWord(word)) {
					continue;
				}
				if (isAnno(word)) {
					continue;
				}
				htmlCode.append(usedVariable(word) + " ");
			}
			isNote(note);
			return true;
		}
		return false;
	}

	/**
	 * 对传入的代码串检查是否存在变量
	 * 
	 * @param word
	 * 
	 * @return 添加变量高亮样式后的代码串
	 */
	public String usedVariable(String word) {
		String used = word;
		for (String field : fields) {
			if (word.contains(field)) {
				used = word.substring(0, word.indexOf(field));
				for (String global : globals) {
					if (used.contains(global)) {
						String front = used;
						used = front.substring(0, front.indexOf(global));
						used += Constants.START_NOBR_GLOBAL + global + Constants.END_NOBR;
						used += usedVariable(front.substring(front.indexOf(global) + global.length()));
						break;
					}
				}
				used += Constants.START_NOBR_FIELD + field + Constants.END_NOBR;
				return used + usedVariable(word.substring(word.indexOf(field) + field.length()));
			}
		}
		for (String global : globals) {
			if (word.contains(global)) {
				used = word.substring(0, word.indexOf(global));
				used += Constants.START_NOBR_GLOBAL + global + Constants.END_NOBR;
				return used + usedVariable(word.substring(word.indexOf(global) + global.length()));
			}
		}
		return used;
	}

	private boolean isDefineMethodOrConstructor(String line) {
		if (line.startsWith("public") || line.startsWith("protected") || line.startsWith("private")) {
			if (line.endsWith("{") && line.contains("(") && line.contains(")")) {
				int start = 0;
				String[] words = line.split(" ");
				String word = words[start];
				htmlCode.append(Constants.START_NOBR_KEY_WORD + word + " ");
				for (start = 1; start < 3 && start < words.length; start++) {
					word = words[start];
					if (word.equals("static") || word.equals("final")) {
						htmlCode.append(word + " ");
					} else {
						break;
					}
				}
				htmlCode.append(Constants.END_NOBR);
				// 判断是否是定义构造函数，因为构造函数不用类型
				if (!words[start].contains("(")) {
					// words[start] ：函数的返回类型是否为基本类型
					if (!isKeyAsBaseType(words[start])) {
						// words[start] ：为引用对象类型
						htmlCode.append(words[start] + " ");
					}
					start++;
				}
				// words[start] ：'函数名' 或 '构造器名'
				if (words[start].contains("(")) {
					word = words[start];
					// 无参函数
					if (word.endsWith("(){") || word.endsWith("()") || word.endsWith("(")) {
						htmlCode.append(word + " ");
						if ((start + 1 < words.length) && words[start + 1].contains("{")) {
							htmlCode.append(words[start + 1]);
						} else {
							htmlCode.append(words[start + 1] + " {");
						}
						return true;
					}
					// 有参函数
					if (word.contains("(") && !word.endsWith("(")) {
						String type = word.substring(word.indexOf("(") + 1);
						htmlCode.append(word.substring(0, word.indexOf("(") + 1));
						if (!isKeyAsWord(type)) {
							htmlCode.append(type + " ");
						}
						start++;
						word = words[start];
						// 一个参数
						if (word.endsWith(")") || word.endsWith("){")) {
							String param = word.substring(0, word.indexOf(")"));
							fields.add(param);
							htmlCode.append(Constants.START_NOBR_FIELD + param + Constants.END_NOBR + ") {");
							return true;
						}
						// 多个参数
						for (; start < words.length; start++) {
							word = words[start];
							if (word.contains(",")) {
								String param = word.substring(0, word.indexOf(","));
								fields.add(param);
								htmlCode.append(Constants.START_NOBR_FIELD + param + Constants.END_NOBR + ", ");
								String type1 = word.substring(word.indexOf(",") + 1);
								if (!isKeyAsWord(type1)) {
									htmlCode.append(type1 + " ");
								}
								continue;
							} else if (word.endsWith(")") || word.endsWith("){")) {
								String param = word.substring(0, word.indexOf(")"));
								fields.add(param);
								htmlCode.append(Constants.START_NOBR_FIELD + param + Constants.END_NOBR + ") {");
								return true;
							} else {
								if (!isKeyAsWord(word)) {
									htmlCode.append(word + " ");
								}
								continue;
							}
						}
					}
				} else {

				}
			}
		}
		return false;
	}

	private boolean isDefineField(String line) {
		//TODO 1.解决局部变量是否为static或final
		//TODO 2.解决定义对象类型局部变量的判断
		if (line.endsWith(";")) {
			String[] words = line.split(" ");
			if (!line.contains("=") && words.length > 2) {
				return false;
			}
			if (isKeyAsBaseType(words[0])) { // 定义基本类型的局部变量
				if (words.length > 1) {
					fields.add(words[1]);
					htmlCode.append(Constants.START_NOBR_FIELD + words[1] + Constants.END_NOBR + " ");
					for (int i = 2; i < words.length; i++) {
						if (isKeyAsWord(words[i])) {
							continue;
						} else if (isString(words, i)) {
							i = stringEndIndex;
							continue;
						}
						htmlCode.append(usedVariable(words[i]) + " ");
					}
				}
				return true;
			} else if (words[0].equals("String")) { // 定义字符串
				htmlCode.append("String ");
				if (words.length > 1) {
					fields.add(words[1]);
					htmlCode.append(Constants.START_NOBR_FIELD + words[1] + Constants.END_NOBR + " ");
					for (int i = 2; i < words.length; i++) {
						if (isKeyAsWord(words[i])) {
							continue;
						} else if (isString(words, i)) {
							i = stringEndIndex;
							continue;
						}
						htmlCode.append(usedVariable(words[i]) + " ");
					}
				}
				return true;
			} else if (words[0].equals("String[]")) { // 定义字符串数组
				htmlCode.append("String[] ");
				if (words.length > 1) {
					fields.add(words[1]);
					htmlCode.append(Constants.START_NOBR_FIELD + words[1] + Constants.END_NOBR + " ");
					for (int i = 2; i < words.length; i++) {
						if (isKeyAsWord(words[i])) {
							continue;
						} else if (isString(words, i)) {
							i = stringEndIndex;
							continue;
						}
						htmlCode.append(usedVariable(words[i]) + " ");
					}
				}
				return true;
			}
		}
		return false;
	}

	private boolean isDefineGlobal(String line) {
		if (line.startsWith("public") || line.startsWith("protected") || line.startsWith("private")) {
			if (line.endsWith(";")) {
				String[] words = line.split(" ");
				int start = 1;
				/*
				 * start ：代码串中除了全局变量基本标志符外其它部分代码分词的起始位置
				 * （代码分词：将代码串通过按空格取出的一组字符串数组）
				 */
				htmlCode.append(Constants.START_NOBR_KEY_WORD + words[0] + " ");
				for (int i = 1; i < words.length; i++) {
					if (words[i].equals("static") || words[i].equals("final")) {
						start++;
						htmlCode.append(words[i] + " ");
					}
				}
				htmlCode.append(Constants.END_NOBR);
				// words[start] ：全局变量的类型是否为基本类型
				if (!isKeyAsBaseType(words[start])) {
					// words[start] ：为引用对象类型
					htmlCode.append(words[start] + " ");
				}
				start++;
				if (isDefineValiableEnd(globals, words, start)) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * 判断定义变量是否结束
	 * @param valiables 变量List<String> 可以是全局变量，也可以是局部变量
	 * @param words
	 * @param start
	 * @return
	 */
	private boolean isDefineValiableEnd(List<String> valiables, String[] words, int start) {
		// 解决一行代码定义多个变量的解析
		// words[start] ：变量名
		if (words[start].endsWith(";")) {
			// 如果以 ; 符号结束，则变量未初始化 ，当前 转换结束
			String name = words[start].substring(0, words[start].lastIndexOf(";"));
			htmlCode.append(Constants.START_NOBR_GLOBAL + name + Constants.END_NOBR + ";");
			valiables.add(name);
			return true;
		} else {
			if (words[start].endsWith(",")) { // 存在同时定义多个变量
				String name = words[start].substring(0, words[start].indexOf(","));
				htmlCode.append(Constants.START_NOBR_GLOBAL + name+ Constants.END_NOBR + ", ");
				valiables.add(name);
				return isDefineValiableEnd(valiables, words, start + 1);
			} else if (words[start + 1].startsWith(",")) {
				htmlCode.append(Constants.START_NOBR_GLOBAL + words[start] + Constants.END_NOBR + ",");
				valiables.add(words[start]);
				if (words[start + 1].length() == 1) {
					return isDefineValiableEnd(valiables, words, start + 2);
				}
				return isDefineValiableEnd(valiables, words, start + 1);
			} else {
				htmlCode.append(Constants.START_NOBR_GLOBAL + words[start] + Constants.END_NOBR + " ");
				valiables.add(words[start]);
				start++;
				// 当前start指向的是变量定义初始化部分的代码分词
				for (int i = start; i < words.length; i++) {
					String word = words[i];
					System.out.println(word);
					if (word.endsWith(",")) { // 存在同时定义多个变量
						String name = words[i].substring(0, word.lastIndexOf(","));
						htmlCode.append(Constants.START_NOBR_GLOBAL + name + Constants.END_NOBR + ",");
						return isDefineValiableEnd(valiables, words, i + 1);
					}
					if (words[i].endsWith(";")) {
						String name = word.substring(0, word.lastIndexOf(";"));
						htmlCode.append(Constants.START_NOBR_GLOBAL + name + Constants.END_NOBR + ";");
						return true;
					}
					if (i + 1 < words.length) {
						if (words[i + 1].startsWith(",")) {
							if (words[i + 1].length() == 1) {
								htmlCode.append(Constants.START_NOBR_GLOBAL + word + Constants.END_NOBR + ",");
								return isDefineValiableEnd(valiables, words, i + 2);
							} else {
								htmlCode.append(Constants.START_NOBR_GLOBAL + word + Constants.END_NOBR + ",");
								return isDefineValiableEnd(valiables, words, i + 1);
							}
						} else {
							if (isKeyAsWord(word)) { // 使用"new"关键字初始化变量
								continue;
							}
							if (isString(words, i)) { // 是否使用到字符串
								i = stringEndIndex; // 指向字符串最后一个单词在代码串分词中的下标
								continue;
							}
							htmlCode.append(usedVariable(word) + " ");
						}
					} else {
						return true;
					}
				}
			}
		}
		return false;
	}

	// 对注解添加代码高亮样式，返回代码串是否属于注解
	public boolean isAnno(String word) {
		// TODO 需要优化包括有赋值注解的情况
		if (word.startsWith("@")) {
			htmlCode.append(Constants.START_NOBR_ANNOTATION + word + Constants.END_NOBR);
			return true;
		}
		return false;
	}

	// 对注释添加代码高亮样式，返回代码串是否属于注释
	public boolean isNote(String line) {
		if (line.startsWith("//")) {
			htmlCode.append(Constants.START_NOBR_NOTE_GREEN + line + Constants.END_NOBR);
			return true;
		}
		if ((line.startsWith("/*") || (line.startsWith(" *") && greenStatus)) && !line.startsWith("/**")) {
			greenStatus = true;
			htmlCode.append(Constants.START_NOBR_NOTE_GREEN + line);
			if (line.endsWith("*/")) {
				greenStatus = false;
			}
			htmlCode.append(Constants.END_NOBR);
			return true;
		}
		if (line.startsWith("/**") || (line.startsWith(" *") && blueStatus)) {
			blueStatus = true;
			htmlCode.append(Constants.START_NOBR_NOTE_BLUE + line);
			if (line.endsWith("*/")) {
				blueStatus = false;
			}
			htmlCode.append(Constants.END_NOBR);
			return true;
		}
		if (line.startsWith(" */") && blueStatus) {
			htmlCode.append(Constants.START_NOBR_NOTE_BLUE + line);
			htmlCode.append(Constants.END_NOBR);
			blueStatus = false;
			return true;
		}
		return false;
	}

	/**
	 * 对代码串分词中属于字符串的代码分词添加string高亮样式
	 * 
	 * @param words
	 *            代码串分词
	 * @param start
	 *            检查字符串在words中的起始下标
	 * @return 是否存在字符串
	 */
	public boolean isString(String[] words, int start) {
		stringEndIndex = start;
		// TODO 字符串可能不只一行代码串 ，可通过添加结束标志boolean来确定下一行的字符串检索
		String word = words[start];
		if (word.startsWith("\"")) { // 字符串的开始标志
			htmlCode.append(Constants.START_NOBR_STRING);

			if (word.lastIndexOf("\"") != 0) { // 字符串是否只包含当前分词
				if (isStringEnd(words, ends, start)) {
					stringEndIndex = start;
				} else {
					htmlCode.append(word + " ");
					start++;
					stringEndIndex = stringEndIndex(words, start); // 从剩余的代码串分词中检索至字符串结束标志
				}
			} else {
				htmlCode.append(word + " ");
				start++;
				stringEndIndex = stringEndIndex(words, start); // 从剩余的代码串分词中检索至字符串结束标志
			}
			return true;
		} else if (word.contains("\"")) { // 当前代码串分词中包含字符串开始标志
			htmlCode.append(usedVariable(word.substring(0, word.indexOf("\""))));
			htmlCode.append(Constants.START_NOBR_STRING);
			words[start] = word.substring(word.indexOf("\""));
			if (words[start].length() > 1 && isStringEnd(words, ends, start)) {
				stringEndIndex = start;
			} else {
				htmlCode.append(words[start] + " ");
				start++;
				stringEndIndex = stringEndIndex(words, start);
			}
			return true;
		} else
			return false;
	}

	// 当前代码行中，字符串是否结束
	private boolean isStringEnd(String[] words, String[] ends, int i) {
		for (String end : ends) {
			if (words[i].endsWith("\"" + end)) { // 字符串结束
				String value = words[i].substring(0, words[i].lastIndexOf(end));
				htmlCode.append(value + Constants.END_NOBR + end);
				return true;
			}
		}
		return false;
	}

	// 如果是字符串，会返回当前代码行字符串结束的下标
	private int stringEndIndex(String[] words, int start) {
		int end = start;
		for (int j = start; j < words.length; j++) {
			if (words[j].contains("\\\"")) { // 字符串包含 \" 符号
				if (isStringEnd(words, ends, j)) {
					return j;
				}
				htmlCode.append(words[j] + " ");
				end = j;
				continue;
			}
			if (isStringEnd(words, ends, j)) {
				return j;
			}
			htmlCode.append(words[j] + " ");
			end = j;
		}
		return end;
	}

	// 对代码串分词为关键字添加key高亮样式
	public boolean isKeyAsWord(String word) {
		switch (word) {
		// 以下是只能单独为一个代码分词的关键字
		case "package":
		case "import":
		case "public":
		case "protected":
		case "private":
		case "class":
		case "interface":
		case "enum":
		case "extends":
		case "implements":
		case "static":
		case "final":
		case "void":
		case "new":
		case "return":
		case "return;":
		case "continue;":
		case "case":
		case "default:":
		case "break;":
			htmlCode.append(Constants.START_NOBR_KEY_WORD + word + Constants.END_NOBR + " ");
			return true;
		}
		// 可能包含其它字符的关键字
		return isKeyAsBaseType(word) || isKeyAsEtc(word) || isKeyAsSentence(word);
	}

	// 关键字为基本类型
	public boolean isKeyAsBaseType(String word) {
		return resoleKey(word, "int") || resoleKey(word, "short") || resoleKey(word, "long")
				|| resoleKey(word, "double") || resoleKey(word, "float") || resoleKey(word, "boolean")
				|| resoleKey(word, "char") || resoleKey(word, "byte");
	}

	// 关键字是语句符号
	public boolean isKeyAsSentence(String word) {
		return resoleKey(word, "if") || resoleKey(word, "else") || resoleKey(word, "else if") || resoleKey(word, "for")
				|| resoleKey(word, "while") || resoleKey(word, "do") || resoleKey(word, "switch");
	}

	// super、this、null、true、false等的关键字
	public boolean isKeyAsEtc(String word) {
		return resoleKey(word, "super") || resoleKey(word, "this") || resoleKey(word, "null") || resoleKey(word, "true")
				|| resoleKey(word, "false") || resoleKey(word, "@interface") || resoleKey(word, "(new")
				|| resoleKey(word, ",new");
	}

	private boolean resoleKey(String word, String type) {
		if (word.contains(type)) {
			int length = word.length(); // 代码串分词长度
			int len = type.length(); // 关键字长度
			if (length == len) {
				// 关键字中不包含其它字符
				htmlCode.append(Constants.START_NOBR_KEY_WORD + word + Constants.END_NOBR + " ");
				return true;
			} else {
				// 包含不是关键字的字符，只高亮关键字
				for (int i = 0; i <= length - len; i++) {
					if (word.substring(i, i + len).equals(type)) {
						htmlCode.append(usedVariable(word.substring(0, i)) + " ");
						htmlCode.append(Constants.START_NOBR_KEY_WORD + type + Constants.END_NOBR);
						if (i != length - len) {
							htmlCode.append(word.substring(i + len));
						}
						htmlCode.append(" ");
						return true;
					}
				}
			}
		}
		return false;
	}

}
