package hu.akarnokd.java9;

import java.awt.Font;
import java.awt.Taskbar;
import java.awt.Taskbar.Feature;

import javax.swing.JFrame;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

public class TaskBarTest {

	static int count;
	
	public static void main(String[] args) throws Exception {
		
		SwingUtilities.invokeLater(() -> {
			Taskbar tb = Taskbar.getTaskbar();

			JFrame w = new JFrame("Taskbar");
			w.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
			w.setVisible(true);
			
			JTextArea p = new JTextArea();

			p.setFont(new Font(Font.SERIF, Font.BOLD, 20));
			
			p.append("Supported:\r\n");
			for (Feature f : Feature.values()) {
				if (tb.isSupported(f)) {
					p.append("  - ");
					p.append(f.toString());
					p.append("\r\n");
				}
			}

			p.append("\r\nNot Supported:\r\n");
			
			for (Feature f : Feature.values()) {
				if (!tb.isSupported(f)) {
					p.append("  - ");
					p.append(f.toString());
					p.append("\r\n");
				}
			}
			
			w.getContentPane().add(p);

			w.setBounds(500, 500, 300, 200);
			
			w.pack();
			
			Timer[] t0 = { null };
			Timer t = new Timer(100, e -> {
				if (count < 33) {
					tb.setWindowProgressState(w, Taskbar.State.ERROR);
				} else
				if (count < 67) {
					tb.setWindowProgressState(w, Taskbar.State.PAUSED);
				} else {
					tb.setWindowProgressState(w, Taskbar.State.NORMAL);
				}
				tb.setWindowProgressValue(w, count++);
				
				if (count > 100) {
					t0[0].stop();
					
					tb.requestWindowUserAttention(w);
				}
			});
			t0[0] = t;
			t.start();
			
		});
	}
}
