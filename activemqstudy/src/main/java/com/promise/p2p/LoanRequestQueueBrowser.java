/** @文件名: LoanRequestQueueBrowser.java @创建人：邢健  @创建日期： 2013-11-29 上午8:11:26 */

package com.promise.p2p;

import java.util.Enumeration;

import javax.jms.Queue;
import javax.jms.QueueBrowser;
import javax.jms.QueueConnection;
import javax.jms.QueueConnectionFactory;
import javax.jms.QueueSession;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.naming.Context;
import javax.naming.InitialContext;

/**   
 * @类名: LoanRequestQueueBrowser.java 
 * @包名: com.promise.p2p 
 * @描述: 分析队列，查看队列
 * @作者: xingjian xingjian@yeah.net   
 * @日期:2013-11-29 上午8:11:26 
 * @版本: V1.0   
 */
public class LoanRequestQueueBrowser {

	public static void main(String[] args) {
		try{
			//创建连接
			Context context = new InitialContext();
			QueueConnectionFactory qcf = (QueueConnectionFactory)context.lookup("QueueCF");
			QueueConnection qc = qcf.createQueueConnection();
			qc.start();
			Queue queue = (Queue)context.lookup("LoanRequestQ");
			QueueSession qs = qc.createQueueSession(false, Session.AUTO_ACKNOWLEDGE);
			QueueBrowser qb = qs.createBrowser(queue);
			Enumeration e = qb.getEnumeration();
			while(e.hasMoreElements()){
				TextMessage tm = (TextMessage)e.nextElement();
				System.out.println("Browsing:"+tm.getText());
			}
			qb.close();
			qc.close();
			System.exit(0);
		}catch(Exception exception){
			exception.printStackTrace();
		}
	}

}
