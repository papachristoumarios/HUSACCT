package husacct.define.presentation;

import husacct.ServiceProvider;
import husacct.common.Resource;
import husacct.common.help.presentation.HelpableJInternalFrame;
import husacct.common.locale.ILocaleService;
import husacct.common.services.IServiceListener;
import husacct.control.ILocaleChangeListener;
import husacct.define.domain.services.WarningMessageService;
import husacct.define.presentation.jdialog.WarningTableJDialog;
import husacct.define.presentation.jpanel.DefinitionJPanel;
import husacct.define.presentation.utils.MessagePanel;
import husacct.define.presentation.utils.ReportToHTML;
import husacct.define.task.DefinitionController;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URISyntaxException;
import java.util.Locale;
import java.util.Observable;
import java.util.Observer;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.WindowConstants;

public class DefineInternalFrame extends HelpableJInternalFrame implements
		ILocaleChangeListener, Observer, IServiceListener {

	private static final long serialVersionUID = 6858870868564931134L;
	private JPanel overviewPanel;
	private DefinitionJPanel definitionPanel;
	private ILocaleService localeService = ServiceProvider.getInstance().getLocaleService();
	private JButton warningButton, reportButton;
	private WarningTableJDialog warnings = new WarningTableJDialog();
	private ReportToHTML report = new ReportToHTML();

	public DefineInternalFrame() {
		super();
		initUi();
	}

	private void initUi() {
		try {
			WarningMessageService.getInstance().addObserver(this);
			setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
			ServiceProvider.getInstance().getDefineService().addServiceListener(this);
			overviewPanel = new JPanel();
			BorderLayout borderLayout = new BorderLayout();
			overviewPanel.setLayout(borderLayout);
			definitionPanel = new DefinitionJPanel();
			overviewPanel.add(definitionPanel);
			getContentPane().add(this.overviewPanel, BorderLayout.CENTER);
			addToolBar();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void addNewDefinitionPanel() {
		DefinitionController.getInstance().clearObserversWithinDefine();
		this.overviewPanel.removeAll();
		definitionPanel = new DefinitionJPanel();
		this.overviewPanel.add(definitionPanel);
		this.revalidate();
	    DefinitionController.getInstance().notifyObservers();
	}

	public DefinitionJPanel getDefinitionPanel() {
		return definitionPanel;
	}
	
	private void addToolBar() {
		JSplitPane splitPane = new JSplitPane();
		splitPane.setDividerLocation(300);
		splitPane.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));

		warningButton = new JButton();
		warningButton.addActionListener(toolbarActionListener);
		warningButton.setText("Warnings");
		Dimension d = warningButton.getPreferredSize();
		d.width = 150;
		warningButton.setMinimumSize(new Dimension(50, 50));
		warningButton.setMaximumSize(d);
		Icon icon = new ImageIcon(Resource.get(Resource.ICON_VALIDATE));
		warningButton.setIcon(icon);
		warningButton.setEnabled(false); // Disabled 2014-06-11, since the shown warnings in the warnings dialogue are not correct

		ImageIcon browserIcon = new ImageIcon(Toolkit.getDefaultToolkit().getImage(Resource.get(Resource.ICON_BROWSER)));
		reportButton = new JButton(localeService.getTranslatedString("ReportTitle"));
		reportButton.setIcon(browserIcon);
		reportButton.setToolTipText(localeService.getTranslatedString("?Report"));
		reportButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					report.createReport();
				} catch (URISyntaxException e1) {
					e1.printStackTrace();
				}
			}
		});

		JPanel buttonPanel = new JPanel();
		buttonPanel.add(warningButton);  
		buttonPanel.add(reportButton);
		
		splitPane.add(buttonPanel, JSplitPane.LEFT);
		splitPane.add(MessagePanel.getInstance(""), JSplitPane.RIGHT);
		getContentPane().add(splitPane, BorderLayout.SOUTH);
	}

	private ActionListener toolbarActionListener = new ActionListener() {
		@Override
		public void actionPerformed(ActionEvent e) {
			if (e.getSource() == warningButton) {
				warnings.refresh();
				warnings.setVisible(true);
			}
		}
	};

	@Override // From ILocaleChangeListener
	public void update(Locale newLocale) {
	}

	@Override
	public void update(Observable o, Object arg) {
		if (WarningMessageService.getInstance().hasWarnings()) {
			Icon icon = new ImageIcon(Resource.get(Resource.ICON_VALIDATE));
			warningButton.setIcon(icon);
		} else {
			warningButton.repaint();
		}
	}

	@Override
	public void update() {
		// Do nothing
	}
}
