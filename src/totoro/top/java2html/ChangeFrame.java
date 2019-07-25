package totoro.top.java2html;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

public class ChangeFrame extends JFrame implements KeyListener{

	private static final long serialVersionUID = 1L;

	public static void main(String[] args) {
		new ChangeFrame();
	}
	
	private JLabel javaLabel = new JLabel("Java：粘贴JAVA代码至下面代码框");
	private JLabel htmlLabel = new JLabel("Html：复制代码框代码到HTML页面");

	private JTextArea javaArea = new JTextArea();
	private JTextArea htmlArea = new JTextArea();
	// 给代码框添加滚动条
	private JScrollPane javaScroll = new JScrollPane(javaArea);
	private JScrollPane htmlScroll = new JScrollPane(htmlArea);
	private JPanel javaPanel = new JPanel();
	private JPanel htmlPanel = new JPanel();
	
	private JButton to = new JButton("转换");
	
	private String t1 = "    ";
	private String javaCode = "";
	
	// 因为代码转换时会有大量的字符串拼接，使用StringBuilder提高拼接效率
	private StringBuilder htmlCode = new StringBuilder(Constants.START_PRE);

	public ChangeFrame() {
		super("Java代码转换为Html代码");
		setBounds(100, 50, 1140, 670);
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		setVisible(true);
		setLayout(null);
		javaPanel.setLayout(null);
		htmlPanel.setLayout(null);
		
		// 代码框自动换行
		javaArea.setLineWrap(true);
		htmlArea.setLineWrap(true);
		// 滚动条一直显示，即使内容没有超过文本框
		javaScroll.setVerticalScrollBarPolicy( JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		htmlScroll.setVerticalScrollBarPolicy( JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		
		javaLabel.setBounds(5, 0, 500, 20);
		htmlLabel.setBounds(5, 0, 500, 20);
		javaScroll.setBounds(5, 20, 500, 600);
		htmlScroll.setBounds(5, 20, 500, 600);
		javaPanel.setBounds(0, 10, 500, 620);
		htmlPanel.setBounds(615, 10, 500, 620);
		to.setBounds(520, 310, 80, 20);
		
		javaPanel.add(javaLabel);
		javaPanel.add(javaScroll);
		htmlPanel.add(htmlLabel);
		htmlPanel.add(htmlScroll);
		add(javaPanel);
		add(htmlPanel);
		add(to);
		repaint();
		
		to.addActionListener((e) -> {
			toHtml();
		});
		javaArea.addKeyListener(this);
	}

	private void toHtml() {
		String[] lines = javaCode.split("\n");
		for (String line : lines) {
			String[] ts = line.split("\t");
			for (String t : ts) {
				if (t.length()==0) {
					htmlCode.append(t1);
				}else {
					// 按空格提取代码词
					String[] words = t.split(" ");
					for (String word : words) {
						if (isCom(word)) {
							continue;
						}
						if (isNote(word)) {
							continue;
						}
						if (isAnno(word)) {
							continue;
						}
						if (isString(word)) {
							continue;
						}
						htmlCode.append(" "+word);
					}
				}
			}
			htmlCode.append(Constants.BR);
		}
		htmlCode.append(Constants.END_PRE);
		htmlArea.setText(htmlCode.toString());
	}

	private boolean isString(String word) {
		if (word.startsWith("\"") || word.endsWith("\"")) {
			htmlCode.append(Constants.START_NOBR_STRING+word+Constants.END_NOBR);
			return true;
		}
		return false;
	}

	private boolean isAnno(String word) {
		if (word.startsWith("@")) {
			htmlCode.append(Constants.START_NOBR_ANNOTATION+word+Constants.END_NOBR);
			return true;
		}
		return false;
	}

	private boolean isNote(String word) {
		boolean a = word.startsWith("//");
		boolean b = word.startsWith("/*");
		boolean c = word.startsWith("/**");
		boolean d = word.startsWith("*");
		boolean e = word.startsWith("*/");
		if (a) {
			htmlCode.append(Constants.START_NOBR_NOTE_GREEN+word+Constants.END_NOBR);
			return true;
		}else if (b && !c) {
			htmlCode.append(Constants.START_NOBR_NOTE_GREEN+word+Constants.END_NOBR);
			return true;
		}else if (c||d||e) {
			htmlCode.append(Constants.START_NOBR_NOTE_BLUE+word+Constants.END_NOBR);
			return true;
		}
		return false;
	}

	private boolean isCom(String word) {
		switch (word) {
		case "package":
		case "import":
		case "public":
		case "protected":
		case "private":
		case "class":
		case "interface":
		case "enum":
		case "@interface":
		case "extends":
		case "static":
		case "final":
		case "int":
		case "short":
		case "long":
		case "double":
		case "float":
		case "void":
		case "true":
		case "(true":
		case "true)":
		case "(true)":
		case "false":
		case "this":
		case "super(":
		case "null":
		case "(null":
		case "null)":
		case "(null)":
		case "if":
		case "if{":
		case "else":
		case "}else":
		case "else{":
		case "}else{":
		case "for":
		case "while":
		case "do":
		case "switch":
		case "case":
		case "default:":
		case "break;":
		case "continue;":
		case "return;":
		case "new":
			htmlCode.append(Constants.START_NOBR_KEY_WORD);
			htmlCode.append(word + Constants.END_NOBR);
			return true;
		
		}
		return false;
	}

	// 监控键盘是否使用粘贴功能，为1时按下ctrl键，为2时，使用粘贴（ctrl+v）
	private int ctrl_v = 0;
	
	@Override
	public void keyReleased(KeyEvent e) {
		int keyCode = e.getKeyCode();
		if ((keyCode == KeyEvent.VK_CONTROL || keyCode == KeyEvent.VK_V) && ctrl_v == 2) {
			ctrl_v = 0;
			new Thread(()->{
				javaCode = javaArea.getText();
				pick(javaCode);
			}).start();;
		}
	}
	
	@Override
	public void keyPressed(KeyEvent e) {
		if(e.getKeyCode() == KeyEvent.VK_CONTROL){
			ctrl_v = 1;
		}
		if(e.getKeyCode() == KeyEvent.VK_V && ctrl_v == 1){
			ctrl_v = 2;
		}
	}

	@Override
	public void keyTyped(KeyEvent e) {
	}
	
	/**
	 * 整理粘贴的内容，以适配代码框
	 * 因为原始的文本框内，制表符的宽度太宽，代码排版相当丑陋
	 * 所以需要对粘贴的内容进行制表符的处理
	 * @param javaCode
	 */
	private void pick(String javaCode) {
		StringBuilder pick = new StringBuilder();
		String[] lines = javaCode.split("\n");
		for (String line : lines) {
			String[] ts = line.split("\t");
			for (String t : ts) {
				if (t.length()==0) { // 当前字段为制表符"\t"
					// 将制表符转化为8个小空格，经过测试8个空格的排版较适合
					pick.append(t1+t1);
				}else {
					pick.append(t);
				}
			}
			pick.append("\n");
		}
		javaArea.setText(pick.toString());
	}

}
