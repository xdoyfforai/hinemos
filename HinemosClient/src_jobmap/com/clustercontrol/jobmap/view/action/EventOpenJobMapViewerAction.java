/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.jobmap.view.action;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ui.IPerspectiveDescriptor;
import org.eclipse.ui.IPerspectiveRegistry;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.IElementUpdater;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.menus.UIElement;
import com.clustercontrol.bean.HinemosModuleConstant;
import com.clustercontrol.jobmanagement.composite.HistoryComposite;
import com.clustercontrol.jobmap.view.JobHistoryViewM;
import com.clustercontrol.monitor.action.GetEventListTableDefine;
import com.clustercontrol.monitor.composite.EventListComposite;
import com.clustercontrol.monitor.view.EventView;
import com.clustercontrol.util.Messages;
import com.clustercontrol.view.ScopeListBaseView;


/**
 * 監視履歴[イベント]からジョブマップビューパースペクティブを開くクライアント側アクションクラス<BR>
 *
 * @version 6.0.0
 * @since 6.0.0
 */
public class EventOpenJobMapViewerAction extends AbstractHandler implements IElementUpdater {
	private IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();

	/** アクションID */
	public static final String ID = BaseAction.ActionIdBase + EventOpenJobMapViewerAction.class.getSimpleName();

	/** ログ */
	private static Log m_log = LogFactory.getLog(EventOpenJobMapViewerAction.class);

	/** ビュー */
	private IWorkbenchPart viewPart;

	/**
	 * Dispose
	 */
	@Override
	public void dispose() {
		this.viewPart = null;
		this.window = null;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.core.commands.IHandler#execute
	 */
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		
		this.window = HandlerUtil.getActiveWorkbenchWindow(event);
		// In case this action has been disposed
		if (null == this.window || !isEnabled()) {
			return null;
		}

		IPerspectiveRegistry reg = PlatformUI.getWorkbench().getPerspectiveRegistry();
		PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		IPerspectiveDescriptor desc = reg.findPerspectiveWithId("com.clustercontrol.enterprise.jobmap.JobMapHistoryPerspective");
		if (desc == null) {
			return null;
		}
		
		// 選択アイテムの取得
		this.viewPart = HandlerUtil.getActivePart(event);
		ScopeListBaseView eventView = null;
		try {
			eventView = (EventView) this.viewPart.getAdapter(EventView.class);
		} catch (Exception e) { 
			m_log.info("execute " + e.getMessage()); 
			return null; 
		}

		if (eventView == null) {
			m_log.info("execute: view is null"); 
			return null;
		}

		EventListComposite composite = (EventListComposite) eventView.getListComposite();
		StructuredSelection  selection = (StructuredSelection) composite.getTableViewer().getSelection();
		
		List<?> list = (ArrayList<?>) selection.getFirstElement();

		if (list == null){
			return null;
		}

		//セッションID（監視ID）を取得
		String monitorId = (String)list.get(GetEventListTableDefine.MONITOR_ID);
		String managerName = (String)list.get(GetEventListTableDefine.MANAGER_NAME);
		String pluginId = (String)list.get(GetEventListTableDefine.PLUGIN_ID);
		
		m_log.debug("monitorId: " + monitorId + ", managerName:" + managerName + ", pluginId:" + pluginId);
		if (!pluginId.equals(HinemosModuleConstant.JOB)) {
			m_log.debug("pluginid is not JOB");
			MessageDialog.openError(null, Messages.getString("error"), 
					com.clustercontrol.jobmap.messages.Messages.getString("message.open.jobviewer.error"));
			return null;
		}

		IWorkbenchPage page = window.getActivePage();

		//パースペクティブを開く
		page.setPerspective(desc);

		JobHistoryViewM historyViewM = (JobHistoryViewM)page.findView(JobHistoryViewM.ID);
		if (historyViewM == null) {
			throw new InternalError("historyViewM is null.");
		}
		
		HistoryComposite historyCmpM = historyViewM.getComposite();

		//ジョブID、ジョブユニットIDを取得
		ArrayList<?> objListM = (ArrayList<?>)historyCmpM.getTableViewer().getInput();
		if (objListM == null || objListM.size() == 0) {
			return null;
		}

		//セッションID、ジョブIDをセット
		historyCmpM.setSessionId(monitorId);

		historyViewM.update(false);

		return null;
	}

	@Override
	public void updateElement(UIElement element, @SuppressWarnings("rawtypes") Map parameters) {
		
		// updateElementはClient本体から呼ぶことが出来ないためコメントアウト
		
//		IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
//		// page may not start at state restoring
//		if( null != window ){
//			IWorkbenchPage page = window.getActivePage();
//			if( null != page ){
//				IWorkbenchPart part = page.getActivePart();
//
//				boolean editEnable = false;
//				if(part instanceof EventView){
//					// Enable button when 1 item is selected
//					EventView view = (EventView)part;
//					EventListComposite composite = (EventListComposite) view.getListComposite();
//					StructuredSelection  selection = (StructuredSelection) composite.getTableViewer().getSelection();
//
//					List<?> list = (ArrayList<?>) selection.getFirstElement();
//
//					if(HinemosModuleConstant.JOB.equals(view.getPluginId()) &&
//							!HinemosModuleConstant.SYSYTEM.equals(list.get(GetEventListTableDefine.MONITOR_ID)) ) {
//						editEnable = true;
//					}
//				}
//				this.setBaseEnabled(editEnable);
//			} else {
//				this.setBaseEnabled(false);
//			}
//		}
	}
}
