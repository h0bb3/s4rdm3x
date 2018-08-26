package se.lnu.siq.s4rdm3x;

//import javafx.scene.input.KeyCode;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;


/**
 * Created by tohto on 2017-08-22.
 */
public class GUIConsole extends JPanel {
    private JFrame m_frame;
    private JTextArea m_output;
    private JTextArea m_input;
    private JSplitPane m_splitPane;

    private ArrayList<String> m_commandList = new ArrayList<>();
    private int m_commandIx = 0;

    private boolean m_hasNewInput;

    private final static String newline = System.getProperty("line.separator");

    public String join(String [] a_parts, int a_startIx, String a_delim) {

        String ret = a_parts[a_startIx];

        for(int sIx = a_startIx + 1; sIx < a_parts.length; sIx++) {
            ret += a_delim + a_parts[sIx];
        }

        return ret;
    }


    public GUIConsole() {

        super(new BorderLayout());

        m_hasNewInput = false;

        m_input = new JTextArea(5, 20);
        m_input.addKeyListener(new KeyListener() {
            @Override
            public void keyTyped(KeyEvent e) {

            }

            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getExtendedKeyCode() == KeyEvent.VK_ENTER) {
                    actionPerformed(null);
                }
            }

            @Override
            public void keyReleased(KeyEvent e) {

            }
        });

        m_output = new JTextArea(5, 20);
        m_output.setEditable(false);

        /*SizeDisplayer sd1 = new SizeDisplayer("left", icon);
        sd1.setMinimumSize(new Dimension(30,30));
        sd1.setFont(font);

        icon = createImageIcon("images/Dog.gif");
        SizeDisplayer sd2 = new SizeDisplayer("right", icon);
        sd2.setMinimumSize(new Dimension(60,60));
        sd2.setFont(font);*/





        //Add Components to this panel.
        GridBagConstraints c = new GridBagConstraints();
        c.gridwidth = GridBagConstraints.REMAINDER;

        c.fill = GridBagConstraints.BOTH;
        c.weightx = 1.0;
        c.weighty = 1.0;
        JScrollPane scrollPane1 = new JScrollPane(m_output);
        scrollPane1.setBorder(null);
        //add(scrollPane, c);

        c.fill = GridBagConstraints.HORIZONTAL;
        c.weighty = 0.0;
        JScrollPane scrollPane2 = new JScrollPane(m_input);
        scrollPane2.setBorder(null);
        //add(scrollPane, c);



        m_splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, scrollPane1, scrollPane2);
        m_splitPane.setResizeWeight(0.5);
        m_splitPane.setOneTouchExpandable(true);
        m_splitPane.setContinuousLayout(true);

        add(m_splitPane, BorderLayout.CENTER);


        // default window style
        String defaultFont = "Lucida Console";
        m_output.setBackground(Color.BLACK);
        m_output.setForeground(Color.LIGHT_GRAY);
        m_output.setCaretColor(Color.WHITE);
        m_output.setFont(new Font(defaultFont, Font.PLAIN, 12));
        m_output.setMargin(new Insets(0, 10, 0, 10));
        //m_output.setBorder(null);

        m_input.setBackground(Color.BLACK);
        m_input.setForeground(Color.LIGHT_GRAY);
        m_input.setCaretColor(Color.WHITE);
        m_input.setFont(new Font(defaultFont, Font.PLAIN, 12));

        //m_input.setBorder(BorderFactory.createLineBorder(Color.black, 2));
        //m_input.setMargin(new Insets(0, 10, 0, 10));
        m_input.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(),
                BorderFactory.createEmptyBorder(0, 10, 3, 10)));
        //






        // default prompt style
        //promptStyle = new SimpleAttributeSet();
        //StyleConstants.setFontFamily(promptStyle, defaultFont);
        //StyleConstants.setFontSize(promptStyle, 12);
        //StyleConstants.setForeground(promptStyle, Color.WHITE);

        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                create();
            }
        });
    }

    private void create() {
        //Create and set up the window.
        JFrame frame = new JFrame("Console");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        //Add contents to the window.
        frame.add(this);


        frame.addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent e) {
                m_input.requestFocusInWindow();
            }

            @Override
            public void focusLost(FocusEvent e) {

            }
        });


        /*javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                Action copy = m_output.getActionMap().get("paste-from-clipboard");

                for (Object o : m_output.getActionMap().keys()) {
                    Sys.out.println((String)o);

                }
            }
        });*/


        m_input.addKeyListener(new KeyListener() {
            @Override
            public void keyTyped(KeyEvent e) {

            }

            @Override
            public void keyPressed(KeyEvent e) {

                if (m_commandList.size() > 0) {
                    if (e.getKeyCode() == KeyEvent.VK_UP) {

                        if (m_commandIx - 1 < m_commandList.size()) {
                            m_commandIx--;
                            if (m_commandIx < 0) {
                                m_commandIx = 0;
                            }
                            m_input.setText(m_commandList.get(m_commandIx));

                        }
                    } else if (e.getKeyCode() == KeyEvent.VK_DOWN) {

                        if (m_commandIx < m_commandList.size()) {
                            m_commandIx++;
                            if (m_commandIx < m_commandList.size()) {
                                m_input.setText(m_commandList.get(m_commandIx));
                            } else {
                                m_input.setText("");
                            }
                        }
                    }
                }
            }

            @Override
            public void keyReleased(KeyEvent e) {

            }
        });

        m_output.addMouseListener(new MouseListener() {
            @Override
            public void mouseClicked(MouseEvent e) {
                m_input.requestFocusInWindow();
            }

            @Override
            public void mousePressed(MouseEvent e) {

            }

            @Override
            public void mouseReleased(MouseEvent e) {

            }

            @Override
            public void mouseEntered(MouseEvent e) {

            }

            @Override
            public void mouseExited(MouseEvent e) {

            }
        });

        m_input.requestFocusInWindow();

        //Display the window.
        frame.pack();
        frame.setSize(480, 640);
        frame.setVisible(true);

    }

    public boolean hasInput() {
        return m_input != null && m_input.getText() != null && m_input.getText().length() > 0 && m_hasNewInput;
    }

    public String popInput() {
        String ret = m_input.getText();
        String parts[] = m_input.getText().split("\n");
        if(parts.length > 1) {
            m_input.setText(join(parts, 1, "\n"));
        } else {
            m_input.setText("");
        }
        m_commandList.add(ret);
        m_commandIx = m_commandList.size();
        m_hasNewInput = parts.length > 1;
        return parts[0].trim();
    }

    public void actionPerformed(ActionEvent evt) {

        m_hasNewInput = m_input.getText().length() > 0;

        String text = m_input.getText();
        println(m_input.getText());
    }

    public void println(String a_str) {
        m_output.append(a_str + newline);
        m_output.selectAll();

        //Make sure the new text is visible, even if there
        //was a selection in the text area.
        m_output.setCaretPosition(m_output.getDocument().getLength() - 1);
    }
}
