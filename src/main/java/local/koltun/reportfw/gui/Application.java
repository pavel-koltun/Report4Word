package local.koltun.reportfw.gui;

import local.koltun.reportfw.docx.FileProcessor;
import local.koltun.reportfw.gui.panel.AboutDialog;
import local.koltun.reportfw.gui.panel.ImagePanel;
import local.koltun.reportfw.gui.resources.LocalizedResourceBundle;
import local.koltun.reportfw.model.PageContentModel;
import org.apache.log4j.Logger;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.concurrent.ExecutionException;

public class Application extends JFrame {
    private static Logger logger = Logger.getLogger(Application.class);
    private final String DEFAULT_IMAGE = "background.png";
    private File recentDirectory = null;

    private JMenuBar menuBar;
    private JMenuItem saveToFile;

    private JProgressBar progressBar;
    private JTextArea textArea;
    private ImagePanel imagePanel;

    private Action addAction;
    private Action removeAction;
    private Action saveAction;
    private Action exitAction;
    private Action saveToFileAction;
    private Action aboutAction;

    private JButton removeButton;
    private JButton saveButton;

    JList<PageContentModel> fileNameList;
    private DefaultListModel<PageContentModel> listModel;
    private boolean isEdited = false;

    private FileProcessor fileProcessor;

    public Application(String title) {
        super(title);

        try{
            this.setIconImage((new ImageIcon(ClassLoader.getSystemResource("app.png"))).getImage());
            UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
            UIManager.getDefaults().put("TextArea.font", UIManager.getFont("TextField.font").deriveFont(12f));

            UIManager.put("OptionPane.yesButtonText", LocalizedResourceBundle.getStringFromBundle("Yes"));
            UIManager.put("OptionPane.okButtonText", LocalizedResourceBundle.getStringFromBundle("Ok"));
            UIManager.put("OptionPane.noButtonText", LocalizedResourceBundle.getStringFromBundle("No"));
            UIManager.put("OptionPane.cancelButtonText", LocalizedResourceBundle.getStringFromBundle("Cancel"));

            UIManager.put("FileChooser.openButtonText", LocalizedResourceBundle.getStringFromBundle("Open"));
            UIManager.put("FileChooser.saveButtonText", LocalizedResourceBundle.getStringFromBundle("Save"));
            UIManager.put("FileChooser.cancelButtonText", LocalizedResourceBundle.getStringFromBundle("Cancel"));
            UIManager.put("FileChooser.filesOfTypeLabelText", LocalizedResourceBundle.getStringFromBundle("FileTypes"));
            UIManager.put("FileChooser.fileNameLabelText", LocalizedResourceBundle.getStringFromBundle("FileNames"));
            UIManager.put("FileChooser.lookInLabelText", LocalizedResourceBundle.getStringFromBundle("LookIn"));
            UIManager.put("FileChooser.saveInLabelText", LocalizedResourceBundle.getStringFromBundle("SaveIn"));
        }
        catch(Exception e){
            logger.error("Exception is:", e);
            e.printStackTrace();
        }

        initActions();
        initMenus();
        initComponents();
    }

    private void initMenus() {
        menuBar = new JMenuBar();

        JMenu fileMenu = new JMenu(LocalizedResourceBundle.getStringFromBundle("File"));
        JMenuItem addImage = new JMenuItem(addAction);
        addImage.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_A, ActionEvent.CTRL_MASK | ActionEvent.SHIFT_MASK));
        saveToFile = new JMenuItem(saveToFileAction);
        saveToFile.setEnabled(false);
        saveToFile.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, ActionEvent.CTRL_MASK));
        JMenuItem exit = new JMenuItem(exitAction);
        exit.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q, ActionEvent.CTRL_MASK));

        fileMenu.add(addImage);
        fileMenu.add(saveToFile);
        fileMenu.addSeparator();
        fileMenu.add(exit);

        JMenu editMenu = new JMenu(LocalizedResourceBundle.getStringFromBundle("Edit"));
        JMenuItem saveItem = new JMenuItem(saveAction);
        saveItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, ActionEvent.CTRL_MASK | ActionEvent.SHIFT_MASK));
        JMenuItem removeItem = new JMenuItem(removeAction);
        removeItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_D, ActionEvent.CTRL_MASK | ActionEvent.SHIFT_MASK));
        editMenu.add(saveItem);
        editMenu.add(removeItem);
        editMenu.setEnabled(false);

        JMenu about = new JMenu(LocalizedResourceBundle.getStringFromBundle("Help"));
        JMenuItem aboutItem = new JMenuItem(aboutAction);
        aboutItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_H, ActionEvent.CTRL_MASK | ActionEvent.SHIFT_MASK));
        about.add(aboutItem);

        menuBar.add(fileMenu);
        menuBar.add(editMenu);
        menuBar.add(about);
    }

    private void initActions() {
        addAction = new AbstractAction(LocalizedResourceBundle.getStringFromBundle("Add")) {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser fileChooser = new JFileChooser(recentDirectory);
                fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
                fileChooser.setMultiSelectionEnabled(true);
                fileChooser.setAcceptAllFileFilterUsed(false);
                fileChooser.addChoosableFileFilter(new FileNameExtensionFilter(LocalizedResourceBundle.getStringFromBundle("ImageDescription"), ImageIO.getReaderFileSuffixes()));
                int option = fileChooser.showOpenDialog(Application.this);
                if (option == JFileChooser.APPROVE_OPTION) {
                    boolean duplicateFileAdded = false;
                    for (File file : fileChooser.getSelectedFiles()) {
                        PageContentModel model = new PageContentModel(file);
                        if (listModel.contains(model)) { //Checks by overrided equals() method
                            duplicateFileAdded = true;
                        } else {
                            listModel.addElement(model);
                        }
                    }
                    if (duplicateFileAdded) {
                        JOptionPane.showMessageDialog(
                                Application.this,
                                "Вы попытались добавить уже загруженные изображения.\nПовторы исключены.",
                                "Повторное добавление",
                                JOptionPane.WARNING_MESSAGE
                        );
                    }
                    setRecentDirectory(fileChooser.getCurrentDirectory());
                }
            }
        };

        removeAction = new AbstractAction(LocalizedResourceBundle.getStringFromBundle("Delete")) {
            @Override
            public void actionPerformed(ActionEvent e) {
                for(int i = 0, count = fileNameList.getSelectedIndices().length; i < count; i++) {
                    listModel.remove(fileNameList.getSelectedIndex());
                }
            }
        };

        saveAction = new AbstractAction(LocalizedResourceBundle.getStringFromBundle("Save")) {
            @Override
            public void actionPerformed(ActionEvent e) {
                setSaveToFileMenuEnabled(true);
                listModel.get(fileNameList.getSelectedIndex()).setDescription(textArea.getText());
            }
        };

        exitAction = new AbstractAction(LocalizedResourceBundle.getStringFromBundle("Exit")) {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (isEdited || fileProcessor != null) {
                    int choice = JOptionPane.showConfirmDialog(
                            Application.this,
                            "Вы действительно желаете выйти из программы?",
                            "Подтверждение выхода",
                            JOptionPane.YES_NO_OPTION,
                            JOptionPane.QUESTION_MESSAGE
                    );
                    if (choice == JOptionPane.NO_OPTION) {
                        return;
                    }
                }
                System.exit(0);
            }
        };

        saveToFileAction = new AbstractAction(LocalizedResourceBundle.getStringFromBundle("Report")) {
            @Override
            public void actionPerformed(ActionEvent e) {
                String fileFormat = "docx";
                JFileChooser fileChooser = new JFileChooser(recentDirectory);
                fileChooser.setFileFilter(new FileNameExtensionFilter(LocalizedResourceBundle.getStringFromBundle("DocumentDescription"), fileFormat));
                fileChooser.setAcceptAllFileFilterUsed(false);

                int choice = fileChooser.showSaveDialog(Application.this);

                if (choice == JFileChooser.APPROVE_OPTION) {
                    File fileSelected = fileChooser.getSelectedFile();
                    if (!fileSelected.getName().endsWith("." + fileFormat)) {
                        fileSelected = new File(fileSelected.getAbsolutePath() + "." + fileFormat);
                    }
                    final File file = fileSelected;

                    if (file.isFile() && file.exists()) {
                        int rewrite = JOptionPane.showConfirmDialog(
                                Application.this,
                                "Перезаписать файл: " + file.getName() + "?",
                                "Сохранить как...",
                                JOptionPane.OK_CANCEL_OPTION
                                );

                        if (rewrite == JOptionPane.CANCEL_OPTION) {
                            return;
                        }
                    }

                    setRecentDirectory(fileChooser.getCurrentDirectory());
                    new Thread("SaveToFile") {
                        @Override
                        public void run() {
                            saveToFile(file);
                        }
                    }.start();
                }
            }
        };

        aboutAction = new AbstractAction(LocalizedResourceBundle.getStringFromBundle("About")) {
            @Override
            public void actionPerformed(ActionEvent e) {
                JOptionPane.showMessageDialog(
                        Application.this, new AboutDialog(),
                        LocalizedResourceBundle.getStringFromBundle("About"),
                        JOptionPane.INFORMATION_MESSAGE);
            }
        };

        this.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                exitAction.actionPerformed(null);
            }
        });
    }

    private void saveToFile(File file) {
        fileProcessor = new FileProcessor(Arrays.asList(listModel.toArray()), file);
        fileProcessor.addPropertyChangeListener(new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                switch (evt.getPropertyName()) {
                    case "progress":
                        progressBar.setIndeterminate(false);
                        progressBar.setValue((Integer) evt.getNewValue());
                        break;
                    case "state":
                        switch ((SwingWorker.StateValue) evt.getNewValue()) {
                            case DONE:
                                progressBar.setVisible(false);
                                SwingUtilities.invokeLater(new Runnable() {
                                    @Override
                                    public void run() {
                                        try {
                                            File newFile = fileProcessor.get();
                                            int choice = JOptionPane.showConfirmDialog(
                                                    Application.this,
                                                    "Файл успешно сохранен.\nОткрыть папку с файлом?",
                                                    "Отчет о сохранении",
                                                    JOptionPane.YES_NO_OPTION,
                                                    JOptionPane.INFORMATION_MESSAGE
                                            );
                                            if (choice == JOptionPane.YES_OPTION) {
                                                new ProcessBuilder("explorer.exe", "/select," + newFile.getAbsolutePath()).start();
                                            }
                                            fileProcessor.cancel(true);
                                            fileProcessor = null;
                                            logger.info("File processor removed.");
                                        } catch (ExecutionException | InterruptedException | IOException e) {
                                            logger.error("Exception is:", e);
                                        }
                                    }
                                });
                                setSaveToFileMenuEnabled(false);
                                break;
                            case STARTED:
                                setSaveToFileMenuEnabled(false);
                                progressBar.setIndeterminate(true);
                                progressBar.setVisible(true);
                                break;
                        }
                }
            }
        });

        fileProcessor.execute();
    }

    private void initComponents() {
        setLayout(new BorderLayout());

        add(menuBar, BorderLayout.NORTH);

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new GridBagLayout());
        GridBagConstraints constraints = generateConstraints(1, 0, 3, 2, 1, 1, new Insets(2, 2, 2, 2), GridBagConstraints.BOTH);
        imagePanel = new ImagePanel();
        mainPanel.add(new JScrollPane(imagePanel), constraints);
        try {
            imagePanel.loadImage(DEFAULT_IMAGE);
        } catch (IOException e) {
            logger.error("Exception is:", e);
        }

        constraints = generateConstraints(1, 2, 2, 2, 1, 0.4, new Insets(2, 2, 2, 2), GridBagConstraints.BOTH);
        textArea = new JTextArea();
        textArea.setEnabled(false);
        mainPanel.add(new JScrollPane(textArea), constraints);

        constraints = generateConstraints(3, 2, 1, 2, 0, 0.4, new Insets(2, 2, 2, 2), GridBagConstraints.BOTH);
        saveButton = new JButton(saveAction);
        saveButton.setEnabled(false);
        mainPanel.add(saveButton, constraints);

        constraints = generateConstraints(0, 0, 1, 2, 0.2, 0.4, new Insets(2, 2, 2, 2), GridBagConstraints.BOTH);
        fileNameList = new JList<>(listModel = new DefaultListModel<>());
        fileNameList.addListSelectionListener(new DefaultListSelectionListener());
        listModel.addListDataListener(new MyListDataListener());

        mainPanel.add(new JScrollPane(fileNameList), constraints);

        constraints = generateConstraints(0, 2, 1, 1, 0.2, 0.2, new Insets(2, 2, 2, 2), GridBagConstraints.BOTH);
        mainPanel.add(new JButton(addAction), constraints);

        constraints = generateConstraints(0, 3, 1, 1, 0.2, 0.2, new Insets(2, 2, 2, 2), GridBagConstraints.BOTH);
        removeButton = new JButton(removeAction);
        removeButton.setEnabled(false);
        mainPanel.add(removeButton, constraints);

        progressBar = new JProgressBar();
        progressBar.setVisible(false);
        constraints = generateConstraints(0, 4, 4, 1, 1, 0.1, new Insets(2, 2, 2, 2), GridBagConstraints.BOTH);
        mainPanel.add(progressBar, constraints);
        add(mainPanel, BorderLayout.CENTER);
    }

    private GridBagConstraints generateConstraints(int gridx, int gridy, int gridwidth, int gridheight, double weightx, double weighty, Insets insets, int fill) {
        return new GridBagConstraints(gridx, gridy, gridwidth, gridheight, weightx, weighty, GridBagConstraints.CENTER, fill, insets, 0, 0);
    }

    private void setRecentDirectory(File recentDirectory) {
        this.recentDirectory = recentDirectory;
    }

    private void setSaveToFileMenuEnabled(boolean enabled) {
        isEdited = enabled;
        saveToFile.setEnabled(isEdited);
    }

    private void setEditEnabled(boolean enabled) {
        textArea.setEnabled(enabled);
        textArea.setText(enabled ? listModel.get(fileNameList.getSelectedIndex()).getDescription() : "");
        saveButton.setEnabled(enabled);
        removeButton.setEnabled(enabled);
        menuBar.getMenu(1).setEnabled(enabled);

        if (enabled) {
            imagePanel.setImage(listModel.get(fileNameList.getSelectedIndex()).getImage());
        }
    }

    class DefaultListSelectionListener implements ListSelectionListener {
        @Override
        public void valueChanged(ListSelectionEvent e) {
            JList sourceList = (JList) e.getSource();
            setEditEnabled(!sourceList.isSelectionEmpty());
        }
    }

    class MyListDataListener implements ListDataListener {
        public void contentsChanged(ListDataEvent e) {
        }

        public void intervalAdded(ListDataEvent e) {
            setSaveToFileMenuEnabled(true);
        }

        public void intervalRemoved(ListDataEvent e) {
            if (listModel.isEmpty()) {
                try {
                    imagePanel.loadImage(DEFAULT_IMAGE);
                    setSaveToFileMenuEnabled(false);
                } catch (IOException exception) {
                    logger.error("Exception is:", exception);
                }
            }
        }
    }
}