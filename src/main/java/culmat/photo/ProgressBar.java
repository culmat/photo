package culmat.photo;

import static javax.swing.SwingUtilities.invokeLater;

import java.awt.BorderLayout;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.SwingWorker;

public class ProgressBar extends JPanel implements PropertyChangeListener {

	private static final long serialVersionUID = 1L;
	private JProgressBar progressBar;
	private final JFrame frame;

	public ProgressBar(JFrame frame) {
		super(new BorderLayout());
		this.frame = frame;

		progressBar = new JProgressBar(0, 100);
		progressBar.setValue(0);

		progressBar.setStringPainted(true);

		JPanel panel = new JPanel();
		panel.add(progressBar);

		add(panel, BorderLayout.PAGE_START);
		setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
	}

	/**
	 * Invoked when task's progress property changes.
	 */
	public void propertyChange(PropertyChangeEvent evt) {
		if ("progress" == evt.getPropertyName()) {
			int progress = (Integer) evt.getNewValue();
			progressBar.setIndeterminate(false);
			progressBar.setValue(progress);
			if (progress >= 100) {
				invokeLater(() -> {
					frame.setVisible(false);
					frame.dispose();
				});
			}
		}
	}

	/**
	 * Create the GUI and show it. As with all GUI code, this must run on the
	 * event-dispatching thread.
	 */
	private static void createAndShowGUI(SwingWorker<?, ?> worker, String title) {
		JFrame frame = new JFrame(title);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		ProgressBar newContentPane = new ProgressBar(frame);
		newContentPane.setOpaque(true); // content panes must be opaque
		frame.setContentPane(newContentPane);

		frame.pack();
		frame.setVisible(true);

		newContentPane.progressBar.setIndeterminate(true);

		worker.addPropertyChangeListener(newContentPane);
		worker.execute();
	}

	public static void main(String[] args) {
		showProgress(new SwingWorker<Void, Void>() {

			@Override
			protected Void doInBackground() throws Exception {
				while (getProgress() < 100) {
					Thread.sleep(30);
					setProgress(getProgress() + 1);
				}
				return null;
			}
		}, "Hi there");
	}

	public static void showProgress(SwingWorker<?, ?> worker, String title) {
		invokeLater(() -> {
			createAndShowGUI(worker, title);
		});
	}
}
