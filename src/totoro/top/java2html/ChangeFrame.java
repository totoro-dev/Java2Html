package totoro.top.java2html;

import java.awt.Dimension;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JViewport;

/**
 * 代码转换可视化界面
 * 
 * 可自适应的界面布局 （@since 1.2)
 * 
 * @author 黄龙三水
 * 
 * @since 1.0
 */
public class ChangeFrame extends JFrame implements KeyListener, ComponentListener {

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

	private JViewport viewport = new JViewport();
	private JPanel base = new JPanel();
	private JScrollPane baseScroll = new JScrollPane(base);

	private JButton to = new JButton("转换");

	private String t1 = "    ";
	private String javaCode = "";

	private Composition composition;

	public ChangeFrame() {
		super("Java代码转换为Html代码");
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		setSize(1140, 680);
		setLocationRelativeTo(null);
		// base.setBackground(Color.BLUE);
		base.setLayout(null);
		javaPanel.setLayout(null);
		htmlPanel.setLayout(null);
		viewport.setLayout(null);
		viewport.setLocation(0, 0);
		setLayout(null);
		setVisible(true);
		// 代码框自动换行
		javaArea.setLineWrap(true);
		htmlArea.setLineWrap(true);
		// 滚动条一直显示，即使内容没有超过文本框
		javaScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		htmlScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		baseScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		baseScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);

		base.setBounds(0, 0, 3000, 2000);
		javaLabel.setBounds(5, 0, 500, 20);
		htmlLabel.setBounds(5, 0, 500, 20);
		javaScroll.setBounds(5, 20, 500, 600);
		htmlScroll.setBounds(5, 20, 500, 600);
		javaPanel.setBounds(0, 0, 505, 625);
		htmlPanel.setBounds(605, 0, 505, 625);
		to.setBounds(520, 300, 70, 30);

		javaPanel.add(javaLabel);
		javaPanel.add(javaScroll);
		htmlPanel.add(htmlLabel);
		htmlPanel.add(htmlScroll);
		base.add(javaPanel);
		base.add(htmlPanel);
		base.add(to);
		base.setPreferredSize(new Dimension(1110, 620));
		viewport.add(base);
		baseScroll.setViewport(viewport);
		add(baseScroll);
		repaint();
		// TODO
		addComponentListener(this);
		to.addActionListener((e) -> {
			composition = new Composition(javaCode);
			htmlArea.setText(composition.toHtml());
		});
		javaArea.addKeyListener(this);
	}

	// 监控键盘是否使用粘贴功能，为1时按下ctrl键，为2时，使用粘贴（ctrl+v）
	private int ctrl_v = 0;

	/**
	 * 整理粘贴的内容，以适配代码框 因为原始的文本框内, 制表符的宽度太宽，代码排版相当丑陋, 所以需要对粘贴的内容进行制表符的处理。
	 * 
	 * @param javaCode
	 */
	private void pick(String javaCode) {
		StringBuilder pick = new StringBuilder();
		String[] lines = javaCode.split("\n");
		for (String line : lines) {
			String[] ts = line.split("\t");
			for (String t : ts) {
				if (t.length() == 0) { // 当前字段为制表符"\t"
					// 将制表符转化为8个小空格，经过测试8个空格的排版较适合
					pick.append(t1 + t1);
				} else {
					pick.append(t);
				}
			}
			pick.append("\n");
		}
		javaArea.setText(pick.toString());
	}

	private int codeWidth, codeHeight, panelWidth, panelHeight, htmlPanelX, htmlPanelY, toX, toY;

	/**
	 * 自适应窗口调整
	 */
	private void resize() {
		int windowWidth = getWidth();
		int windowHeight = getHeight();
		baseScroll.setSize(windowWidth - 15, windowHeight - 40);

		if (windowWidth > windowHeight) {
			if (windowWidth <= 630) {
				windowWidth = 600;
				windowHeight = 600;
				base.setPreferredSize(new Dimension(600, 680));
				vertical(windowWidth, windowHeight);
			} else {
				if (windowHeight < 600) {
					windowHeight = 600;
					base.setPreferredSize(new Dimension(windowWidth - 33, 625));
				} else {
					base.setPreferredSize(new Dimension(windowWidth - 33, windowHeight - 60));
					windowHeight -= 80;
				}
				windowWidth -= 140;
				horizontal(windowWidth, windowHeight);
			}
		} else {
			if (windowHeight <= 680) {
				windowWidth = 800;
				windowHeight = 600;
				base.setPreferredSize(new Dimension(910, 625));
				horizontal(windowWidth, windowHeight);
			} else {
				windowHeight -= 142;
				if (windowWidth < 630) {
					windowWidth = 600;
					base.setPreferredSize(new Dimension(600, windowHeight - 60));
				} else {
					windowWidth -= 30;
					base.setPreferredSize(new Dimension(windowWidth - 33, windowHeight - 60));
				}
				vertical(windowWidth, windowHeight);
			}
		}

		javaLabel.setBounds(5, 0, codeWidth, 20);
		htmlLabel.setBounds(5, 0, codeWidth, 20);
		javaScroll.setBounds(5, 20, codeWidth, codeHeight);
		htmlScroll.setBounds(5, 20, codeWidth, codeHeight);
		javaPanel.setBounds(0, 0, panelWidth, panelHeight);
		htmlPanel.setBounds(htmlPanelX, htmlPanelY, panelWidth, panelHeight);
		to.setBounds(toX, toY, 70, 30);
		repaint();
	}

	private void horizontal(int width, int height) {
		codeWidth = width / 2;
		codeHeight = height;
		panelWidth = codeWidth + 5;
		panelHeight = codeHeight + 25;
		htmlPanelX = panelWidth + 100;
		htmlPanelY = 0;
		toX = panelWidth + 15;
		toY = height / 2;
	}

	private void vertical(int width, int height) {
		codeWidth = width - 5;
		codeHeight = height / 2;
		panelWidth = codeWidth + 5;
		panelHeight = codeHeight + 20;
		htmlPanelX = 0;
		htmlPanelY = panelHeight + 50;
		toX = width / 2 - 35;
		toY = panelHeight + 10;
	}

	@Override
	public void keyReleased(KeyEvent e) {
		int keyCode = e.getKeyCode();
		if ((keyCode == KeyEvent.VK_CONTROL || keyCode == KeyEvent.VK_V) && ctrl_v == 2) {
			ctrl_v = 0;
			new Thread(() -> {
				javaCode = javaArea.getText();
				pick(javaCode);
			}).start();
			;
		}
	}

	@Override
	public void keyPressed(KeyEvent e) {
		if (e.getKeyCode() == KeyEvent.VK_CONTROL) {
			ctrl_v = 1;
		}
		if (e.getKeyCode() == KeyEvent.VK_V && ctrl_v == 1) {
			ctrl_v = 2;
		}
	}

	@Override
	public void keyTyped(KeyEvent e) {
	}

	@Override
	public void componentHidden(ComponentEvent e) {
	}

	@Override
	public void componentMoved(ComponentEvent e) {
		resize();
	}

	@Override
	public void componentResized(ComponentEvent e) {
		resize();
	}

	@Override
	public void componentShown(ComponentEvent e) {
		resize();
	}
}
