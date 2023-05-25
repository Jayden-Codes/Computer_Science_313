import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.WindowConstants;
import javax.swing.border.LineBorder;

public class ProgressBar {
    private JProgressBar receiverPB;
    private JButton pauseBtn;
    private boolean paused = false;

/**
 * Progress Bar constructor.
 * @param parentFrame - Parent componet.
 * @param title - Name of Frame.
 */
    public ProgressBar(JFrame parentFrame, String title) {
        JDialog dialog = new JDialog(new JFrame(), title);
        dialog.setLocationRelativeTo(parentFrame);
        receiverPB = new JProgressBar();
        receiverPB.setStringPainted(true);
        receiverPB.setBorder(new LineBorder(Color.DARK_GRAY));
        receiverPB.setForeground(Color.green);
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(receiverPB, BorderLayout.CENTER);
        pauseBtn = new JButton("Pause");
        pauseBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                paused = !paused;
                if(paused) {
                    pauseBtn.setText("Resume");
                } else {
                    pauseBtn.setText("Pause");
                }
            }
        });
        /* Add Progress bar is called by receiver. */
        if (title.contains("RECEIVER")) {
             panel.add(pauseBtn, new BorderLayout().SOUTH);
        }
        dialog.add(panel);
        dialog.setSize(200, 100);
        dialog.setLocationRelativeTo(parentFrame);
        dialog.setVisible(true);
        dialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
    }
/**
 * Updates progress bar value.
 * @param percent - value progress bar set to.
 */
    public void setProgress(int percent) {
        receiverPB.setValue(percent);
    }

    /*
     * Refresh progress bar.
     */
    public void update() {
        receiverPB.repaint();
        receiverPB.validate();
    }
    /*
     * Get if progress bar is paused
     */
    public boolean getPaused() {
        return paused;
    }

}
